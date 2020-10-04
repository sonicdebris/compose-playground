package sonicdebris.composepanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import kotlin.random.Random

data class Levels(val l: Float, val r: Float)
val SILENCE = Levels(0f, 0f)

class Panner(
    val scope: CoroutineScope
) {

    private var _started = MutableLiveData(false)
    val started: LiveData<Boolean> = _started
    var job: Job? = null

    fun toggle() {

        val  j = job
        if (j == null) {

            job = scope.launch {
                _inputLevels.value = Levels(0.5f, 0.5f)
                try {
                    while (isActive) {
                        delay(35)
                        fakeProcess()
                    }
                } catch(c: CancellationException) {}
                _inputLevels.value = SILENCE
                _outputLevels.value = SILENCE
                _started.value = false
            }

            _started.value = true
        } else {
            j.cancel()
            job = null
        }
    }

    private fun fakeProcess() {

        // input level is brownian noise:
        val (lPrev, rPrev) = _inputLevels.value ?: SILENCE
        val l = brownian(lPrev)
        val r = brownian(rPrev)

        _inputLevels.value = Levels(l, r)
        val g = _gain.value ?: 0f
        val lPanGain = ((1 - (_pan.value ?: 0.5f)) * 2).coerceIn(0f, 1f)
        val rPanGain = ((_pan.value ?: 0.5f) * 2).coerceIn(0f, 1f)
        _outputLevels.value = Levels(l * g * lPanGain, r * g * rPanGain)
    }

    private fun brownian(prev: Float) = (prev + (Random.nextFloat() - 0.5f) * 0.1f).coerceIn(0f, 1f)

    private var _gain = MutableLiveData(1f)
    val gain: LiveData<Float> = _gain
    fun updateGain(g: Float) {
        _gain.value = g.coerceIn(0f, 1f)
    }

    private var _pan = MutableLiveData(0.5f)
    val pan: LiveData<Float> = _pan
    fun updatePan(p: Float) {
        _pan.value = p.coerceIn(0f, 1f)
    }

    private var _inputLevels = MutableLiveData(SILENCE)
    val inputLevels: LiveData<Levels> = _inputLevels

    private var _outputLevels = MutableLiveData(SILENCE)
    val outputLevels: LiveData<Levels> = _outputLevels
}
