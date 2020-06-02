package com.example.myapp.util

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView


class TouchImageView : AppCompatImageView, GestureDetector.OnGestureListener,
    OnDoubleTapListener {
    var mtrx: Matrix? = null
    var mode = NONE

    // Remember some things for zooming
    var last = PointF()
    var start = PointF()
    var minScale = 1f
    var maxScale = 3f
    lateinit var m: FloatArray
    var viewWidth = 0
    var viewHeight = 0
    var saveScale = 1f
    protected var origWidth = 0f
    protected var origHeight = 0f
    var oldMeasuredWidth = 0
    var oldMeasuredHeight = 0
    var mScaleDetector: ScaleGestureDetector? = null
    @get:JvmName("getContext_")
    private var context: Context? = null

    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        sharedConstructing(context)
    }

    var mGestureDetector: GestureDetector? = null
    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        this.context = context
        mGestureDetector = GestureDetector(context, this)
        mGestureDetector!!.setOnDoubleTapListener(this)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mtrx = Matrix()
        m = FloatArray(9)
        imageMatrix = mtrx
        scaleType = ScaleType.MATRIX
        setOnTouchListener { v, event ->
            mScaleDetector!!.onTouchEvent(event)
            mGestureDetector!!.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val deltaX = curr.x - last.x
                    val deltaY = curr.y - last.y
                    val fixTransX = getFixDragTrans(
                        deltaX, viewWidth.toFloat(),
                        origWidth * saveScale
                    )
                    val fixTransY = getFixDragTrans(
                        deltaY, viewHeight.toFloat(),
                        origHeight * saveScale
                    )
                    mtrx?.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    last[curr.x] = curr.y
                }
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK) performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            imageMatrix = mtrx
            invalidate()
            true // indicate event was handled
        }
    }

    fun setMaxZoom(x: Float) {
        maxScale = x
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        // Double tap is detected
        Log.i("MAIN_TAG", "Double tap detected")
        val origScale = saveScale
        val mScaleFactor: Float
        if (saveScale == maxScale) {
            saveScale = minScale
            mScaleFactor = minScale / origScale
        } else {
            saveScale = maxScale
            mScaleFactor = maxScale / origScale
        }
        mtrx?.postScale(
            mScaleFactor, mScaleFactor, viewWidth / 2.toFloat(),
            viewHeight / 2.toFloat()
        )
        fixTrans()
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }
            if (origWidth * saveScale <= viewWidth
                || origHeight * saveScale <= viewHeight
            ) mtrx!!.postScale(
                mScaleFactor, mScaleFactor, viewWidth / 2.toFloat(),
                viewHeight / 2.toFloat()
            ) else mtrx!!.postScale(
                mScaleFactor, mScaleFactor,
                detector.focusX, detector.focusY
            )
            fixTrans()
            return true
        }
    }

    fun fixTrans() {
        mtrx!!.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]
        val fixTransX =
            getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(
            transY, viewHeight.toFloat(), origHeight
                    * saveScale
        )
        if (fixTransX != 0f || fixTransY != 0f) mtrx!!.postTranslate(fixTransX, fixTransY)
    }

    fun getFixTrans(
        trans: Float,
        viewSize: Float,
        contentSize: Float
    ): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) return -trans + minTrans
        return if (trans > maxTrans)
            maxTrans - trans
        else 0f
    }

    fun getFixDragTrans(
        delta: Float,
        viewSize: Float,
        contentSize: Float
    ): Float {
        return if (contentSize <= viewSize) {
            0f
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight || viewWidth == 0 || viewHeight == 0
        ) return
        oldMeasuredHeight = viewHeight
        oldMeasuredWidth = viewWidth
        if (saveScale == 1f) {
            // Fit to screen.
            val scale: Float
            val drawable = drawable
            if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0
            ) return
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight
            Log.d("bmSize", "bmWidth: $bmWidth bmHeight : $bmHeight")
            val scaleX = viewWidth.toFloat() / bmWidth.toFloat()
            val scaleY = viewHeight.toFloat() / bmHeight.toFloat()
            scale = Math.min(scaleX, scaleY)
            mtrx!!.setScale(scale, scale)

            // Center the image
            var redundantYSpace = (viewHeight.toFloat()
                    - scale * bmHeight.toFloat())
            var redundantXSpace = (viewWidth.toFloat()
                    - scale * bmWidth.toFloat())
            redundantYSpace /= 2.toFloat()
            redundantXSpace /= 2.toFloat()
            mtrx!!.postTranslate(redundantXSpace, redundantYSpace)
            origWidth = viewWidth - 2 * redundantXSpace
            origHeight = viewHeight - 2 * redundantYSpace
            imageMatrix = mtrx
        }
        fixTrans()
    }

    companion object {
        // We can be in one of these 3 states
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
        const val CLICK = 3
    }
}