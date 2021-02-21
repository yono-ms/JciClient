package com.example.jciclient

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
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

    private val vOutCallback = object : IVLCVout.Callback {
        override fun onSurfacesCreated(vlcVout: IVLCVout?) {
            logger.info("onSurfacesCreated")
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
        }
    }

    private val mediaPlayerEventLister = MediaPlayer.EventListener { event ->
        when (event?.type) {
            MediaPlayer.Event.EndReached -> {
                logger.info("EndReached")
                viewModel.playing.value = false
                mediaPlayer.stop()
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
            MediaPlayer.Event.PositionChanged -> {
                logger.trace("PositionChanged ${mediaPlayer.position}")
                viewModel.position.value = mediaPlayer.position
            }
            MediaPlayer.Event.TimeChanged -> {
                logger.trace("TimeChanged ${mediaPlayer.time} ${mediaPlayer.length}")
                viewModel.time.value = mediaPlayer.time
                viewModel.length.value = mediaPlayer.length
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

            surfaceView = binding.surfaceView
            surfaceHolder = binding.surfaceView.holder
            surfaceHolder.setKeepScreenOn(true)

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

            binding.imageButtonRotate.setOnClickListener {
                activity?.requestedOrientation?.let { orientation ->
                    activity?.requestedOrientation = when (orientation) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
                viewModel.rotation.value = when (activity?.requestedOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> 180F
                    else -> 0F
                }
            }

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val position = progress.toFloat() / VideoViewerViewModel.SEEK_BAR_MAX
                        mediaPlayer.position = position
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    mediaPlayer.pause()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    mediaPlayer.play()
                }
            })

            viewModel.uriString.observe(viewLifecycleOwner) {
                setupPlayer()
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
        hideBars()
    }

    private fun setupPlayer() {
        kotlin.runCatching {
            libVLC = LibVLC(activity, vlcOptions)

            mediaPlayer = MediaPlayer(libVLC).apply {
                setEventListener(mediaPlayerEventLister)
            }

            // Setting up video output
            mediaPlayer.vlcVout.apply {
                setVideoView(surfaceView)
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