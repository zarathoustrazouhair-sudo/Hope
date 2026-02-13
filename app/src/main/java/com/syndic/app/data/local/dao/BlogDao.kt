package com.syndic.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syndic.app.data.local.entity.BlogPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlogDao {
    @Query("SELECT * FROM blog_posts ORDER BY date DESC")
    fun getAllPosts(): Flow<List<BlogPostEntity>>

    @Query("SELECT * FROM blog_posts WHERE id = :id")
    suspend fun getPostById(id: String): BlogPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: BlogPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<BlogPostEntity>)

    @Query("DELETE FROM blog_posts WHERE id = :postId")
    suspend fun deletePost(postId: String)
}
