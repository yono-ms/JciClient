package com.example.jciclient

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jciclient.databinding.VideoViewerFragmentBinding
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class VideoViewerFragment : BaseFragment() {

    private val args by navArgs<VideoViewerFragmentArgs>()

    private val viewModel by viewModels<VideoViewerViewModel> {
        VideoViewerViewModel.Factory(
            args.remoteId,
            args.path
        )
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    private val vlcOptions by lazy {
        ArrayList<String>().apply {
            add("--aout=opensles")
            add("--http-reconnect")
            add("--audio-time-stretch") // time stretching
            add("--network-caching=1500")
            add("-vvv") // verbosity
        }
    }

    lateinit var binder: BridgeService.BridgeBinder

    private val vOutCallback = object : IVLCVout.Callback {
        override fun onSurfacesCreated(vlcVout: IVLCVout?) {
            val sw = surfaceView.width
            val sh = surfaceView.height

            if (sw * sh == 0) {
                logger.error("Invalid surface size")
                return
            }

            mediaPlayer.vlcVout.setWindowSize(sw, sh)
            //mediaPlayer.aspectRatio = "4:3"
            mediaPlayer.scale = 0f

        }

        override fun onSurfacesDestroyed(vlcVout: IVLCVout?) {
            releasePlayer()
            binder.stopWebServer()
        }
    }

    private val mediaPlayerEventLister = MediaPlayer.EventListener { event ->
        when (event?.type) {
            MediaPlayer.Event.EndReached -> {
                logger.info("EndReached")
                releasePlayer()
                viewModel.playing.value = false
            }
            MediaPlayer.Event.Playing -> {
                logger.info("Playing")
                viewModel.playing.value = true
            }
            MediaPlayer.Event.Paused -> {
                logger.info("Paused")
                viewModel.playing.value = false
            }
            MediaPlayer.Event.Stopped -> {
                logger.info("Stopped")
                viewModel.playing.value = false
            }
            else -> logger.trace("${event?.type}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logger.info("onCreateView")
        return VideoViewerFragmentBinding.inflate(inflater).also { binding ->
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner

            kotlin.runCatching {
                libVLC = LibVLC(activity, vlcOptions)

                surfaceView = binding.surfaceView
                surfaceHolder = binding.surfaceView.holder
                surfaceHolder.setKeepScreenOn(true)

                mediaPlayer = MediaPlayer(libVLC).apply {
                    setEventListener(mediaPlayerEventLister)
                }

                // Setting up video output
                mediaPlayer.vlcVout.apply {
                    setVideoView(binding.surfaceView)
                    addCallback(vOutCallback)
                    attachViews()
                }

            }.onSuccess {
                logger.info("success.")
            }.onFailure {
                logger.error("onCreateView", it)
                findNavController().navigate(
                    VideoViewerFragmentDirections.actionGlobalMessageDialogFragment(
                        "${it.message}"
                    )
                )
            }

            binding.surfaceView.setOnClickListener {
                viewModel.toggleControlVisible()
            }

            binding.imageButtonPlay.setOnClickListener {
                viewModel.playing.value?.let {
                    if (it) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.play()
                    }
                }
            }

            viewModel.uriString.observe(viewLifecycleOwner) {
                mediaPlayer.media = Media(libVLC, Uri.parse(it))
                mediaPlayer.play()
            }

            viewModel.throwable.observe(viewLifecycleOwner) {
                findNavController().navigate(
                    HomeFragmentDirections.actionGlobalMessageDialogFragment(
                        it.message.toString()
                    )
                )
            }

            viewModel.progress.observe(viewLifecycleOwner) {
                binding.progressBar.isVisible = it
            }

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.info("onViewCreated")
        if (savedInstanceState == null) {
            viewModel.startWebServer()
        }
    }

    override fun onResume() {
        super.onResume()
        val words = args.path.split("/")
        val title = words.lastOrNull()
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    private fun releasePlayer() {
        logger.info("releasePlayer")
        kotlin.runCatching {
            mediaPlayer.stop()
            mediaPlayer.vlcVout.let {
                it.removeCallback(vOutCallback)
                it.detachViews()
            }
            mediaPlayer.release()
            libVLC.release()
        }.onFailure {
            logger.error("releasePlayer", it)
        }
    }
}