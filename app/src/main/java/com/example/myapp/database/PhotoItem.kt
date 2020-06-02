package com.example.myapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "photo")
data class PhotoItem(

	@PrimaryKey
	@field:SerializedName("id")
	var id: String = "0",

	@field:SerializedName("owner")
	var owner: String? = null,

	@field:SerializedName("server")
	var server: String? = null,

	@field:SerializedName("ispublic")
	var public: Int? = null,

	@field:SerializedName("isfriend")
	var friend: Int? = null,

	@field:SerializedName("farm")
	var farm: Int? = null,

	@field:SerializedName("secret")
	var secret: String? = null,

	@field:SerializedName("title")
	var title: String? = null,

	@field:SerializedName("isfamily")
	var family: Int? = null

) {
	var indexInResponse = -1

	var searchText = ""

	fun getThumbnail(): String {
		return "https://farm$farm.staticflickr.com/$server/${id}_${secret}_m.jpg"
	}
}
