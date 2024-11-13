package com.dh.galleryapp.core.data.repository.mock

import androidx.paging.PagingData
import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.model.Image
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class MockRepositoryImpl @Inject constructor() : Repository {

    override fun loadImageList(): Flow<PagingData<Image>> {
        val items = listOf(
            Image(
                id = "0",
                url = "https://unsplash.com/photos/yC-Yzbqy7PY",
                downloadUrl = "https://picsum.photos/id/0/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "1",
                url = "https://unsplash.com/photos/LNRyGwIJr5c",
                downloadUrl = "https://picsum.photos/id/1/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "2",
                url = "https://unsplash.com/photos/N7XodRrbzS0",
                downloadUrl = "https://picsum.photos/id/2/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "3",
                url = "https://unsplash.com/photos/Dl6jeyfihLk",
                downloadUrl = "https://picsum.photos/id/3/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "4",
                url = "https://unsplash.com/photos/y83Je1OC6Wc",
                downloadUrl = "https://picsum.photos/id/4/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "5",
                url = "https://unsplash.com/photos/LF8gK8-HGSg",
                downloadUrl = "https://picsum.photos/id/5/5000/3334",
                width = 5000,
                height = 3334,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "6",
                url = "https://unsplash.com/photos/tAKXap853rY",
                downloadUrl = "https://picsum.photos/id/6/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "7",
                url = "https://unsplash.com/photos/BbQLHCpVUqA",
                downloadUrl = "https://picsum.photos/id/7/4728/3168",
                width = 4728,
                height = 3168,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "8",
                url = "https://unsplash.com/photos/xII7efH1G6o",
                downloadUrl = "https://picsum.photos/id/8/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "9",
                url = "https://unsplash.com/photos/ABDTiLqDhJA",
                downloadUrl = "https://picsum.photos/id/9/5000/3269",
                width = 5000,
                height = 3269,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "10",
                url = "https://unsplash.com/photos/6J--NXulQCs",
                downloadUrl = "https://picsum.photos/id/10/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "11",
                url = "https://unsplash.com/photos/Cm7oKel-X2Q",
                downloadUrl = "https://picsum.photos/id/11/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "12",
                url = "https://unsplash.com/photos/I_9ILwtsl_k",
                downloadUrl = "https://picsum.photos/id/12/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "13",
                url = "https://unsplash.com/photos/3MtiSMdnoCo",
                downloadUrl = "https://picsum.photos/id/13/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "14",
                url = "https://unsplash.com/photos/IQ1kOQTJrOQ",
                downloadUrl = "https://picsum.photos/id/14/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "15",
                url = "https://unsplash.com/photos/NYDo21ssGao",
                downloadUrl = "https://picsum.photos/id/15/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "16",
                url = "https://unsplash.com/photos/gkT4FfgHO5o",
                downloadUrl = "https://picsum.photos/id/16/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "17",
                url = "https://unsplash.com/photos/Ven2CV8IJ5A",
                downloadUrl = "https://picsum.photos/id/17/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "18",
                url = "https://unsplash.com/photos/Ps2n0rShqaM",
                downloadUrl = "https://picsum.photos/id/18/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "19",
                url = "https://unsplash.com/photos/P7Lh0usGcuk",
                downloadUrl = "https://picsum.photos/id/19/2500/1667",
                width = 2500,
                height = 1667,
                author = "Paul Jarvis"
            ),
            Image(
                id = "20",
                url = "https://unsplash.com/photos/nJdwUHmaY8A",
                downloadUrl = "https://picsum.photos/id/20/3670/2462",
                width = 3670,
                height = 2462,
                author = "Aleks Dorohovich"
            ),
            Image(
                id = "21",
                url = "https://unsplash.com/photos/jVb0mSn0LbE",
                downloadUrl = "https://picsum.photos/id/21/3008/2008",
                width = 3008,
                height = 2008,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "22",
                url = "https://unsplash.com/photos/du_OrQAA4r0",
                downloadUrl = "https://picsum.photos/id/22/4434/3729",
                width = 4434,
                height = 3729,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "23",
                url = "https://unsplash.com/photos/8yqds_91OLw",
                downloadUrl = "https://picsum.photos/id/23/3887/4899",
                width = 3887,
                height = 4899,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "24",
                url = "https://unsplash.com/photos/cZhUxIQjILg",
                downloadUrl = "https://picsum.photos/id/24/4855/1803",
                width = 4855,
                height = 1803,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "25",
                url = "https://unsplash.com/photos/Iuq0EL4EINY",
                downloadUrl = "https://picsum.photos/id/25/5000/3333",
                width = 5000,
                height = 3333,
                author = "Alejandro Escamilla"
            ),
            Image(
                id = "26",
                url = "https://unsplash.com/photos/tCICLJ5ktBE",
                downloadUrl = "https://picsum.photos/id/26/4209/2769",
                width = 4209,
                height = 2769,
                author = "Vadim Sherbakov"
            ),
            Image(
                id = "27",
                url = "https://unsplash.com/photos/iJnZwLBOB1I",
                downloadUrl = "https://picsum.photos/id/27/3264/1836",
                width = 3264,
                height = 1836,
                author = "Yoni Kaplan-Nadel"
            ),
            Image(
                id = "28",
                url = "https://unsplash.com/photos/_WiFMBRT7Aw",
                downloadUrl = "https://picsum.photos/id/28/4928/3264",
                width = 4928,
                height = 3264,
                author = "Jerry Adney"
            ),
            Image(
                id = "29",
                url = "https://unsplash.com/photos/V0yAek6BgGk",
                downloadUrl = "https://picsum.photos/id/29/4000/2670",
                width = 4000,
                height = 2670,
                author = "Go Wild"
            )
        )

        return flow {
            delay(3000)
            emit(
                PagingData.from(
                    items
                )
            )
        }
    }


    override suspend fun downloadImage(url: String, filePath: String): Result<File> {
        return Result.success(
            File(filePath)
        )
    }
}


