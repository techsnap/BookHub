package com.mypc.bookhub.model

import java.io.Serializable

data class Book(
    var bookId: String,
    var bookName: String,
    var bookAuthor: String,
    var publishedDate: String,
    var bookPages: String,
    var bookPrice: String,
    var bookImage: String
): Serializable