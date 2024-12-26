package com.example.storyapp.view.main

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.api.ListStoryItem
import com.example.storyapp.loginwithanimation.databinding.ItemStoryBinding
import com.example.storyapp.view.detail.DetailStory
import com.example.storyapp.view.detail.DetailStory.Companion.STORY_ID

class StoryAdapter : ListAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            Glide.with(binding.root.context)
                .load(story.photoUrl)
                .into(binding.ivStoryPhoto)
            binding.tvStoryName.text = story.name
            binding.tvStoryDescription.text = story.description

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStory::class.java).apply {
                    putExtra(STORY_ID, story) // Mengirim data Parcelable
                }

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        androidx.core.util.Pair(binding.ivStoryPhoto, "image"),
                        androidx.core.util.Pair(binding.tvStoryName, "title"),
                        androidx.core.util.Pair(binding.tvStoryDescription, "desc")
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }
}
