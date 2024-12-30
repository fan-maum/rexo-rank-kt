package com.fanmaum.lexorank_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fanmaum.lexorank.LexoRank
import java.security.SecureRandom

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val reorderableLexoranks = remember {
                mutableStateListOf(Pair(generateRandomString(), LexoRank.middle().toString()))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                TextButton(
                    onClick = {
                        /** Add an item to the end of the list, the [LexoRank.genNext] function is used to generate the next rank value. **/
                        reorderableLexoranks.add(
                            Pair(
                                generateRandomString(),
                                LexoRank(reorderableLexoranks[reorderableLexoranks.lastIndex].second)
                                    .genNext()
                                    .toString()
                            )
                        )
                    }
                ) {
                    Text("add item")
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(reorderableLexoranks) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.first)
                            Text("Key: ${item.second}")
                            TextButton(
                                enabled = index != 0,
                                onClick = {
                                    val targetIndex = index - 1

                                    reorderableLexoranks.move(index, targetIndex)

                                    val newKey =
                                        if (targetIndex == 0) {
                                            /** When moving to the beginning of the list, the [LexoRank.genPrev] function is used to generate the previous rank value. **/
                                            LexoRank(reorderableLexoranks[index].second).genPrev()
                                        } else {
                                            /** When moving between items, the [LexoRank.between] function is used to generate the in-between rank value. **/
                                            LexoRank(reorderableLexoranks[targetIndex - 1].second).between(
                                                LexoRank(reorderableLexoranks[targetIndex + 1].second)
                                            )
                                        }

                                    reorderableLexoranks[targetIndex] =
                                        reorderableLexoranks[targetIndex].copy(second = newKey.toString())
                                }
                            ) {
                                Text("up")
                            }
                            TextButton(
                                enabled = index != reorderableLexoranks.lastIndex,
                                onClick = {
                                    val targetIndex = index + 1

                                    reorderableLexoranks.move(index, targetIndex)

                                    val newKey =
                                        if (targetIndex == reorderableLexoranks.lastIndex) {
                                            /** When moving to the end of the list, the [LexoRank.genNext] function is used to generate the next rank value. **/
                                            LexoRank(reorderableLexoranks[index].second).genNext()
                                        } else {
                                            /** When moving between items, the [LexoRank.between] function is used to generate the in-between rank value. **/
                                            LexoRank(reorderableLexoranks[targetIndex - 1].second).between(
                                                LexoRank(reorderableLexoranks[targetIndex + 1].second)
                                            )
                                        }

                                    reorderableLexoranks[targetIndex] =
                                        reorderableLexoranks[targetIndex].copy(second = newKey.toString())
                                }
                            ) {
                                Text("down")
                            }
                        }
                    }
                }
            }
        }
    }

    /* generate random item name */
    private fun generateRandomString(): String {
        val random = SecureRandom()
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8)
            .map { charset[random.nextInt(charset.length)] }
            .joinToString("")
    }

    private fun <T> MutableList<T>.move(from: Int, to: Int) {
        if (from == to) return
        val element = this.removeAt(from) ?: return
        this.add(to, element)
    }
}