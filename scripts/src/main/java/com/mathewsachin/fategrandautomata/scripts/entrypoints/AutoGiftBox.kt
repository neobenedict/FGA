package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.ScriptExitException
import javax.inject.Inject
import kotlin.time.seconds

class AutoGiftBox @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    companion object {
        const val maxClickCount = 99
        const val maxNullStreak = 3
    }

    override fun script(): Nothing {
        var clickCount = 0
        var aroundEnd = false
        var nullStreak = 0

        while (clickCount < maxClickCount) {
            val picked = useSameSnapIn {
                if (!aroundEnd) {
                    // The scrollbar end position matches before completely at end
                    // a few items can be left off if we're not careful
                    aroundEnd = images.giftBoxScrollEnd in game.giftBoxScrollEndRegion
                }

                pickGifts()
            }

            clickCount += picked

            swipe(
                game.giftBoxSwipeStart,
                game.giftBoxSwipeEnd
            )

            if (aroundEnd) {
                // Once we're around the end, stop after we don't pick anything consecutively
                if (picked == 0) {
                    ++nullStreak
                } else nullStreak = 0

                if (nullStreak >= maxNullStreak) {
                    break
                }

                // Longer animations. At the end, items pulled up and released.
                1.seconds.wait()
            }
        }

        /*
           clickCount can be higher than maxClickCount when the script is close to the limit and
           finds multiple collectible stacks on the screen. FGO will not register the extra clicks.
         */
        throw ScriptExitException(messages.pickedExpStack(clickCount.coerceAtMost(maxClickCount)))
    }

    // Return picked count
    private fun pickGifts(): Int {
        var clickCount = 0

        for (gift in game.giftBoxCheckRegion.findAll(images.giftBoxCheck).sorted()) {
            val offset = Location(0, gift.Region.Y)

            val countRegion = game.giftBoxCountRegion + offset
            val iconRegion = game.giftBoxIconRegion + offset
            val clickSpot = game.giftBoxClickSpot + offset

            val gold = images.goldXP in iconRegion
            val silver = !gold && images.silverXP in iconRegion

            if (gold || silver) {
                if (gold) {
                    val count = mapOf(
                        1 to images.x1,
                        2 to images.x2,
                        3 to images.x3,
                        4 to images.x4
                    ).entries.firstOrNull { (_, pattern) ->
                        countRegion.exists(pattern, Similarity = 0.87)
                    }?.key

                    if (count == null || count > prefs.maxGoldEmberSetSize) {
                        continue
                    }
                }

                clickSpot.click()
                ++clickCount
            }
        }

        return clickCount
    }
}