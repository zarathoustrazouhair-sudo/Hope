package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "blog_posts")
data class BlogPostEntity(
    @PrimaryKey
    val id: String, // UUID
    val title: String,
    val content: String,
    val authorId: String, // References UserEntity.id
    val date: Date,
    val category: String = "Annonce" // e.g. "Annonce", "Événement"
)
