package com.dh.galleryapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dh.galleryapp.feature.detail.DetailScreen
import com.dh.galleryapp.feature.list.ListScreen
import com.dh.galleryapp.ui.navigation.Navigation
import com.dh.galleryapp.ui.navigation.argumentThumbnailKey
import com.dh.galleryapp.ui.navigation.argumentUrl
import java.net.URLEncoder

@Composable
fun GalleryApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Navigation.List.name, modifier = modifier) {
        composable(Navigation.List.name) {
            ListScreen(
                modifier = Modifier.fillMaxSize(),
                onItemClick = { url, thumbnailKey ->
                    navController.navigate(
                        Navigation.Detail.name + "/${
                            URLEncoder.encode(
                                url,
                                "utf-8"
                            )
                        }/$thumbnailKey",
                    )
                }
            )
        }

        composable(
            Navigation.Detail.name + "/{$argumentUrl}/{$argumentThumbnailKey}",
            arguments = listOf(
                navArgument(argumentUrl) {
                    type = NavType.StringType
                },
                navArgument(argumentThumbnailKey) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString(argumentUrl)!!
            val thumbnailKey = URLEncoder.encode(
                backStackEntry.arguments?.getString(argumentThumbnailKey)!!,
                "utf-8"
            )

            DetailScreen(
                modifier = Modifier.fillMaxSize(),
                url = url,
                thumbnailKey = thumbnailKey,
            )
        }
    }
}