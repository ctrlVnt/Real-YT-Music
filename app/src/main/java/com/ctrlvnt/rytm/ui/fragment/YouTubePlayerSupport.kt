package com.ctrlvnt.rytm.ui.fragment


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.ctrlvnt.rytm.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


class YouTubePlayerSupport : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.youtube_player, container, false)

        val videoId = arguments?.getString("video_id")

        val youTubePlayerView: YouTubePlayerView = rootView.findViewById(R.id.youtube_player_view)
        //viewLifecycleOwner.lifecycle.addObserver(youTubePlayerView) //consigliato
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayerView.enableBackgroundPlayback(true) //background music, not legal
                videoId?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        })

        return rootView
    }

    companion object {
        fun newInstance(videoId: String): YouTubePlayerSupport {
            val fragment = YouTubePlayerSupport()
            val args = Bundle()
            args.putString("video_id", videoId)
            fragment.arguments = args
            return fragment
        }
    }

    private fun showNotification() {
        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Controlla se è necessario creare un canale di notifica (per Android Oreo e successivi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "your_channel_id",
                "Your Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(requireContext(), "your_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_background) // Icona della notifica
            .setContentTitle("Titolo della notifica")
            .setContentText("Testo della notifica")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notification: Notification = builder.build()

        // Mostra la notifica
        notificationManager.notify(1, notification) // Puoi cambiare il numero della notifica (1) a tuo piacimento
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Chiamata per mostrare la notifica
        showNotification()
    }
}
