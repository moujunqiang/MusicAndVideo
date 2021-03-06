package com.yizisu.music.and.video.module.fragment.search


import android.os.Bundle
import android.view.LayoutInflater
import com.yizisu.basemvvm.mvvm.mvvm_helper.LiveBean
import com.yizisu.basemvvm.mvvm.mvvm_helper.LiveBeanStatus
import com.yizisu.basemvvm.mvvm.mvvm_helper.registerLiveBean
import com.yizisu.basemvvm.utils.getResString
import com.yizisu.basemvvm.utils.launchThread
import com.yizisu.music.and.roomdblibrary.DbCons
import com.yizisu.music.and.roomdblibrary.DbHelper
import com.yizisu.music.and.roomdblibrary.bean.AlbumInfoTable
import com.yizisu.music.and.roomdblibrary.bean.SongInfoTable
import com.yizisu.music.and.video.AppData
import com.yizisu.music.and.video.R
import com.yizisu.music.and.video.baselib.base.BaseFragment
import com.yizisu.music.and.video.bean.dongwo.SearchBean
import com.yizisu.music.and.video.module.search.adapter.SearchAdapter
import com.yizisu.music.and.video.utils.dbViewModel
import com.yizisu.music.and.video.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search.*


class SearchFragment : BaseFragment() {
    companion object {
        val titles by lazy {
            mutableMapOf(
                DbCons.SOURCE_LOCAL to getResString(R.string.local_music),
                DbCons.SOURCE_NETEASE to getResString(R.string.netease_music),
                DbCons.SOURCE_BAIDU to getResString(R.string.baidu_music),
                DbCons.SOURCE_KUGOU to getResString(R.string.kugou_music),
                DbCons.SOURCE_MIGU to getResString(R.string.migu_music)
            )
        }

        fun create(type: Int): SearchFragment {
            return SearchFragment().apply {
                sourceType = type
            }
        }
    }

    private var sourceType: Int? = null
    private val searchAdapter = SearchAdapter(null)
    private val searchViewModel by lazy { getActivityViewModel<SearchViewModel>() }
    override fun getContentResOrView(inflater: LayoutInflater): Any? = R.layout.fragment_search


    override fun initUi(savedInstanceState: Bundle?) {
        super.initUi(savedInstanceState)
        searchRcv.adapter = searchAdapter
    }

    private fun addNewSongToAlbum(song: SongInfoTable, album: AlbumInfoTable) {
        launchThread {
            DbHelper.addSongToAlbum(song, album)
            when (album.dbId) {
                DbCons.ALBUM_ID_HEART -> {
                    dbViewModel.queryHeartList()
                }
                else -> {
                }
            }
        }
    }


    /**
     * 处理加载成功
     */
    private fun <T> loadSuccess(data: LiveBean<T>, onSuccess: (LiveBean<T>) -> Unit) {
        when (data.status) {
            LiveBeanStatus.START -> {
                showLoadingView()
                searchAdapter.clearDatas()
            }
            LiveBeanStatus.SUCCESS -> {
                showContentView()
                onSuccess.invoke(data)
            }
            LiveBeanStatus.FAIL -> {
                showOtherView(data.errorMsg)
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        when (sourceType) {
            //搜索网易云
            DbCons.SOURCE_NETEASE -> {
                searchViewModel?.neteaseSearchData?.let { bean ->
                    registerLiveBean(bean) {
                        loadSuccess(it) {
                            refreshAdapter(searchViewModel?.neteaseToSearchBean(it.data))
                        }
                    }
                }

            }
            //搜索百度
            DbCons.SOURCE_BAIDU -> {
                searchViewModel?.baiduSearchData?.let {
                    registerLiveBean(it) {
                        loadSuccess(it) {
                            refreshAdapter(searchViewModel?.baiduToSearchBean(it.data))
                        }
                    }
                }
            }
            DbCons.SOURCE_KUGOU -> {
                searchViewModel?.kugouSearchData?.let { bean ->
                    registerLiveBean(bean) {
                        loadSuccess(it) {
                            refreshAdapter(searchViewModel?.kugouToSearchBean(it.data))
                        }
                    }
                }
            }
            DbCons.SOURCE_MIGU -> {
                searchViewModel?.nodeJsMiguSearchData?.let { bean ->
                    registerLiveBean(bean) {
                        loadSuccess(it) {
                            refreshAdapter(searchViewModel?.nodeJsMiguToSearchBean(it.data))
                        }
                    }
                }
            }
            DbCons.SOURCE_LOCAL -> {
                searchViewModel?.localSearchData?.let { bean ->
                    registerLiveBean(bean) {
                        loadSuccess(it) {
                            refreshAdapter(it.data)
                        }
                    }
                }
            }
        }
    }

    private var keywords: String? = null
    private var isResume = false
    fun search(words: String?) {
        keywords = words
        if (isResume) {
            onFirstVisible()
        }
    }

    override fun onFirstVisible() {
        super.onFirstVisible()
        isResume = true
        when (sourceType) {
            //搜索网易云
            DbCons.SOURCE_NETEASE -> {
                searchViewModel?.searchByNetease(keywords)
            }
            //搜索百度
            DbCons.SOURCE_BAIDU -> {
                searchViewModel?.searchByBaidu(keywords)
            }
            DbCons.SOURCE_KUGOU -> {
                searchViewModel?.searchByKugou(keywords)
            }
            DbCons.SOURCE_MIGU -> {
                searchViewModel?.searchByMessapiMigu(keywords)
            }
            DbCons.SOURCE_LOCAL -> {
                searchViewModel?.searchByLocal(keywords)
            }
        }
    }

    override fun isNeedSwitchView(): Boolean {
        return true
    }

    private fun refreshAdapter(bean: SearchBean?) {
        if (bean?.songInfoTables.isNullOrEmpty()) {
            showOtherView("什么都没搜到呢")
        } else {
            if (sourceType != DbCons.SOURCE_LOCAL) {
                bean?.songInfoTables?.forEach { search ->
                    AppData.dbDownloadAlbumData.data?.songInfoTables?.forEach { down ->
                        if (search.id == down.id &&
                            search.source == down.source
                        ) {
                            search.playFilePath = down.playFilePath
                            search.playUrlPath = down.playUrlPath
                        }
                    }
                }
            }
            searchAdapter.refreshList(bean?.songInfoTables)
        }
    }

    override fun getTitle(): CharSequence? {
        return titles[sourceType]
    }

}
