package com.apollo.thefragments.ui.camera

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apollo.thefragments.R
import com.apollo.thefragments.data.model.Photo
import io.getstream.photoview.PhotoView

class PhotoViewerAdapter(
    private val photos: List<Photo>
) : RecyclerView.Adapter<PhotoViewerAdapter.ViewHolder>() {

    // Use the inflated item view as the ViewHolder.itemView
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoView: PhotoView = itemView.findViewById(R.id.photoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the full item layout and pass it to ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_viewer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Safely decode file (may be null) and set into PhotoView
        val bitmap = BitmapFactory.decodeFile(photos[position].localPath)
        holder.photoView.setImageBitmap(bitmap) // if bitmap == null, this clears the image
    }

    override fun getItemCount() = photos.size
}
