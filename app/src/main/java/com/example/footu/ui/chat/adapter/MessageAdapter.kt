package com.example.footu.ui.chat.adapter

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.footu.R
import com.example.footu.Response.MessageResponse
import java.io.File

class MessageAdapter :
    ListAdapter<MessageResponse, RecyclerView.ViewHolder>(object :
        DiffUtil.ItemCallback<MessageResponse>() {
        override fun areItemsTheSame(oldItem: MessageResponse, newItem: MessageResponse): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(
            oldItem: MessageResponse,
            newItem: MessageResponse,
        ): Boolean {
            return oldItem == newItem
        }
    }) {

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
        const val IMAGE_SENDER = 3
        const val IMAGE_RECEIVER = 4
        const val VIDEO_SENDER = 5
        const val VIDEO_RECEIVER = 6
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(p0.context)
        return when (p1) {
            VIEW_TYPE_ONE -> {
                val view = inflater.inflate(R.layout.send_chat_layout, p0, false)
                SenderViewHolder(view)
            }

            VIEW_TYPE_TWO -> {
                val view = inflater.inflate(R.layout.receive_chat_layout, p0, false)
                ReceiverViewHolder(view)
            }

            IMAGE_SENDER -> {
                val view = inflater.inflate(R.layout.send_image_layout, p0, false)
                ImageSenderViewHolder(view)
            }

            IMAGE_RECEIVER -> {
                val view = inflater.inflate(R.layout.receive_image_layout, p0, false)
                ImageReceiverViewHolder(view)
            }

            VIDEO_SENDER -> {
                val view = inflater.inflate(R.layout.send_video_layout, p0, false)
                VideoSenderViewHolder(view)
            }

            VIDEO_RECEIVER -> {
                val view = inflater.inflate(R.layout.receive_video_layout, p0, false)
                VideoReceiverViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.empty, p0, false)
                EmptyView(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: MessageResponse = getItem(position)
        when (holder) {
            is SenderViewHolder -> {
                holder.tvMessage.text = message.content
                holder.timeOfMessage.text = message.time
            }

            is ReceiverViewHolder -> {
                holder.tvMessage.text = message.content
                holder.timeOfMessage.text = message.time
            }

            is ImageSenderViewHolder -> {
                val byteArray = Base64.decode(message.content, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Glide.with(holder.itemView.context)
                    .load(bitmap)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?,
                        ) {
                            holder.img.setImageDrawable(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Do nothing
                        }
                    })
            }

            is ImageReceiverViewHolder -> {
                val byteArray = Base64.decode(message.content, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Glide.with(holder.itemView.context)
                    .load(bitmap)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?,
                        ) {
                            holder.img.setImageDrawable(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Do nothing
                        }
                    })
            }

            is VideoSenderViewHolder -> {
                holder.bindView(message)
            }

            is VideoReceiverViewHolder -> {
                holder.bindView(message)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: MessageResponse = getItem(position)
        return if (message.isSendByUser) {
            if (message.type == 0) VIEW_TYPE_ONE else if (message.type == 1) IMAGE_SENDER else VIDEO_SENDER
        } else {
            if (message.type == 0) VIEW_TYPE_TWO else if (message.type == 1) IMAGE_RECEIVER else VIDEO_RECEIVER
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        when (holder) {
            is VideoReceiverViewHolder -> {
                holder.releaseExoPlayer()
            }

            is VideoSenderViewHolder -> {
                holder.releaseExoPlayer()
            }

            else -> {
            }
        }
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.send_message)
        var timeOfMessage: TextView = itemView.findViewById(R.id.time_message_send)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.receive_message)
        var timeOfMessage: TextView = itemView.findViewById(R.id.time_message_receive)
    }

    inner class ImageSenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_mess)
    }

    inner class ImageReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_mess_receive)
    }

    inner class VideoReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerView: PlayerView = itemView.findViewById(R.id.video_mess_receive)
        val pbLoading: ProgressBar = itemView.findViewById(R.id.pbLoading)
        private val exoPlayer = ExoPlayer.Builder(itemView.rootView.context).build()
        fun bindView(message: MessageResponse) {
            val byteArray = Base64.decode(message.content, Base64.DEFAULT)
            val tempFile = File.createTempFile("tempVideo", ".mp4", itemView.context.cacheDir)
            tempFile.writeBytes(byteArray)

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(
                        itemView.rootView.context,
                        "Can't play this video",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_BUFFERING) {
                        pbLoading.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        pbLoading.visibility = View.GONE
                    }
                }
            })

            playerView.player = exoPlayer

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(itemView.rootView.context)

            val mediaSource = Uri.fromFile(tempFile)?.let { MediaItem.fromUri(it) }
//                ?.let {
//                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
//                    it,
//                )
//            }

            mediaSource?.let { exoPlayer.setMediaItem(it) }
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }
        }

        fun releaseExoPlayer() {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    inner class VideoSenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerView: PlayerView = itemView.findViewById(R.id.video_mess_send)
        val pbLoading: ProgressBar = itemView.findViewById(R.id.pbLoading)
        private val exoPlayer = ExoPlayer.Builder(itemView.rootView.context).build()

        fun bindView(message: MessageResponse) {
            val byteArray = Base64.decode(message.content, Base64.DEFAULT)
            val tempFile = File.createTempFile("tempVideo", ".mp4", itemView.context.cacheDir)
            tempFile.writeBytes(byteArray)

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(
                        itemView.rootView.context,
                        "Can't play this video",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_BUFFERING) {
                        pbLoading.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        pbLoading.visibility = View.GONE
                    }
                }
            })

            playerView.player = exoPlayer

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(itemView.rootView.context)

            val mediaSource = Uri.fromFile(tempFile)?.let { MediaItem.fromUri(it) }
//                ?.let {
//                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
//                    it,
//                )
//            }

            mediaSource?.let { exoPlayer.setMediaItem(it) }
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }
        }

        fun releaseExoPlayer() {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    inner class EmptyView(itemView: View) : RecyclerView.ViewHolder(itemView)
}
