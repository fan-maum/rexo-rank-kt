package com.fanmaum.lexorank_sample

import android.os.Bundle
import android.util.Log
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
            val reorderableBiases = remember {
                mutableStateListOf(Pair(generateRandomString(), "0|hzzzzz:"))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                TextButton(
                    onClick = {
                        /** 리스트의 마지막 부분에 [LexoRank.genNext] 함수를 통해 다음 순위값을 생성 **/
                        reorderableBiases.add(
                            Pair(
                                generateRandomString(),
                                LexoRank(reorderableBiases[reorderableBiases.lastIndex].second)
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
                    itemsIndexed(reorderableBiases) { index, item ->
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

                                    reorderableBiases.move(index, targetIndex)

                                    val newKey =
                                        if (targetIndex == 0) {
                                            /** 리스트의 처음으로 이동할 경우 [LexoRank.genPrev] 함수를 통해 이전 순위값을 생성 **/
                                            LexoRank(reorderableBiases[index].second).genPrev()
                                        } else {
                                            /** 아이템의 사이로 이동할 경우 [LexoRank.between] 함수를 통해 사이 순위값을 생성 **/
                                            LexoRank(reorderableBiases[targetIndex - 1].second).between(
                                                LexoRank(reorderableBiases[targetIndex + 1].second)
                                            )
                                        }

                                    reorderableBiases[targetIndex] =
                                        reorderableBiases[targetIndex].copy(second = newKey.toString())
                                }
                            ) {
                                Text("up")
                            }
                            TextButton(
                                enabled = index != reorderableBiases.lastIndex,
                                onClick = {
                                    val targetIndex = index + 1

                                    reorderableBiases.move(index, targetIndex)

                                    val newKey =
                                        if (targetIndex == reorderableBiases.lastIndex) {
                                            /* 리스트의 마지막으로 이동할 경우 다음 값을 생성 합니다.*/
                                            LexoRank(reorderableBiases[index].second).genNext()
                                        } else {
                                            /** 아이템의 사이로 이동할 경우 [LexoRank.between] 함수를 통해 사이 순위값을 생성 **/
                                            LexoRank(reorderableBiases[targetIndex - 1].second).between(
                                                LexoRank(reorderableBiases[targetIndex + 1].second)
                                            )
                                        }

                                    reorderableBiases[targetIndex] =
                                        reorderableBiases[targetIndex].copy(second = newKey.toString())
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

    /* 아이템에 랜덤 네이밍 부여 */
    private fun generateRandomString(
        length: Int = 8,
        charset: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    ): String {
        val random = SecureRandom()
        return (1..length)
            .map { charset[random.nextInt(charset.length)] }
            .joinToString("")
    }

    private fun <T> MutableList<T>.move(from: Int, to: Int) {
        if (from == to) return
        val element = this.removeAt(from) ?: return
        this.add(to, element)
    }
}