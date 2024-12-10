package com.dh.galleryapp.feature.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState

@Composable
fun rememberIsError(
    state: CombinedLoadStates,
): Boolean {
    var isShowingRetry by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(state) {
        isShowingRetry = state.refresh is LoadState.Error ||
                state.append is LoadState.Error
    }

    return isShowingRetry
}