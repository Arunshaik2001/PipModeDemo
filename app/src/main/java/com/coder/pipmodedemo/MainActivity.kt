package com.coder.pipmodedemo

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.coder.pipmodedemo.databinding.ActivityMainBinding


private const val ACTION_TIMER_CONTROL = "time_control"

private const val CONTROL_TYPE = "type"
private const val CONTROL_TYPE_START_OR_PAUSE = 2

private const val START_OR_PAUSE_TIME = 4


class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != ACTION_TIMER_CONTROL) {
                return
            }
            when (intent.getIntExtra(CONTROL_TYPE, 0)) {
                CONTROL_TYPE_START_OR_PAUSE -> viewModel.startOrPause()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.startOrPause.setOnClickListener { viewModel.startOrPause() }
        viewModel.time.observe(this) { time -> binding.time.text = time }
        viewModel.started.observe(this) { started ->
            binding.startOrPause.setImageResource(
                if (started) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp
            )
            updatePictureInPictureParams(started)
        }
        registerReceiver(broadcastReceiver, IntentFilter(ACTION_TIMER_CONTROL))
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            binding.startOrPause.visibility = View.GONE
        } else {
            binding.startOrPause.visibility = View.VISIBLE
        }
    }

    private fun updatePictureInPictureParams(started: Boolean): PictureInPictureParams {
        val visibleRect = Rect()
        binding.timerBackground.getGlobalVisibleRect(visibleRect)
        val params = PictureInPictureParams.Builder()
            .setActions(
                listOf(
                    if (started) {
                        createRemoteAction(
                            R.drawable.ic_pause_24dp,
                            R.string.pause,
                            START_OR_PAUSE_TIME,
                            CONTROL_TYPE_START_OR_PAUSE
                        )
                    } else {
                        createRemoteAction(
                            R.drawable.ic_play_arrow_24dp,
                            R.string.start,
                            START_OR_PAUSE_TIME,
                            CONTROL_TYPE_START_OR_PAUSE
                        )
                    }
                )
            )
            .setAspectRatio(Rational(16, 9))
            .setSourceRectHint(visibleRect)
            .setAutoEnterEnabled(true)
            .setSeamlessResizeEnabled(false)
            .build()
        setPictureInPictureParams(params)
        return params
    }


    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        @StringRes titleResId: Int,
        requestCode: Int,
        controlType: Int
    ): RemoteAction {
        return RemoteAction(
            Icon.createWithResource(this, iconResId),
            getString(titleResId),
            getString(titleResId),
            PendingIntent.getBroadcast(
                this,
                requestCode,
                Intent(ACTION_TIMER_CONTROL)
                    .putExtra(CONTROL_TYPE, controlType),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        viewModel.started.observe(this) { started ->
            binding.startOrPause.setImageResource(
                if (started) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp
            )
            enterPictureInPictureMode(updatePictureInPictureParams(started))
        }
    }
}