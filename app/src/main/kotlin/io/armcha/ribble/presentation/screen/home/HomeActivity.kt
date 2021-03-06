package io.armcha.ribble.presentation.screen.home

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.View
import io.armcha.ribble.R
import io.armcha.ribble.domain.entity.User
import io.armcha.ribble.presentation.base_mvp.base.BaseActivity
import io.armcha.ribble.presentation.screen.about.AboutFragment
import io.armcha.ribble.presentation.screen.auth.AuthActivity
import io.armcha.ribble.presentation.screen.shot_root.ShotRootFragment
import io.armcha.ribble.presentation.screen.user_following.UserFollowingFragment
import io.armcha.ribble.presentation.screen.user_likes.UserLikesFragment
import io.armcha.ribble.presentation.utils.extensions.*
import io.armcha.ribble.presentation.utils.glide.TransformationType
import io.armcha.ribble.presentation.utils.glide.load
import io.armcha.ribble.presentation.widget.navigation_view.NavigationItem
import io.armcha.ribble.presentation.widget.navigation_view.NavigationItemSelectedListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import javax.inject.Inject
import io.armcha.ribble.presentation.widget.navigation_view.NavigationId as Id


class HomeActivity : BaseActivity<HomeContract.View, HomeContract.Presenter>(), HomeContract.View,
        NavigationItemSelectedListener {

    private val TRANSLATION_X_KEY = "TRANSLATION_X_KEY"
    private val CARD_ELEVATION_KEY = "CARD_ELEVATION_KEY"
    private val SCALE_KEY = "SCALE_KEY"

    @Inject
    protected lateinit var homePresenter: HomePresenter

    override fun initPresenter() = homePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(io.armcha.ribble.R.layout.activity_home)
        initViews()

        presenter.getNavigatorState()?.let {
            navigator.restore(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        fun put(key: String, value: Float) = outState?.putFloat(key, value)
        with(mainView) {
            put(TRANSLATION_X_KEY, translationX)
            put(CARD_ELEVATION_KEY, scale)
            put(SCALE_KEY, cardElevation)
        }
    }

    override fun onRestoreInstanceState(savedState: Bundle?) {
        super.onRestoreInstanceState(savedState)
        savedState?.let {
            with(mainView) {
                translationX = it.getFloat(TRANSLATION_X_KEY)
                scale = it.getFloat(CARD_ELEVATION_KEY)
                cardElevation = it.getFloat(SCALE_KEY)
            }
        }
    }

    override fun onDestroy() {
        presenter.saveNavigatorState(navigator.getState())
        super.onDestroy()
    }

    override fun injectDependencies() {
        activityComponent.inject(this)
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        navView.navigationItemSelectListener = this
        navView.header.userName

        drawerLayout.drawerElevation = 0F
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val moveFactor = navView.width * slideOffset
                mainView.translationX = moveFactor
                mainView.scale = 1 - slideOffset / 4
                mainView.cardElevation = slideOffset * 10.toPx(this@HomeActivity)
            }

            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                presenter.handleDrawerOpen()
            }

            override fun onDrawerClosed(drawerView: View?) {
                super.onDrawerClosed(drawerView)
                presenter.handleDrawerClose()
            }
        })
        drawerLayout.setScrimColor(Color.TRANSPARENT)

//        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
//            v.addTopMargin(insets.systemWindowInsetTop)
//            navView.addBottomMargin(insets.systemWindowInsetBottom)
//            insets
//        }
    }

    override fun setArcArrowState() {
        arcView.onClick {
            super.onBackPressed()
        }
        arcImage.setAnimatedImage(io.armcha.ribble.R.drawable.arrow_left)
    }

    override fun setArcHamburgerIconState() {
        drawerLayout?.let {
            arcView.onClick {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            arcImage.setAnimatedImage(io.armcha.ribble.R.drawable.hamb)
        }
    }

    override fun openShotFragment() {
        goTo<ShotRootFragment>()
    }

    override fun openLoginActivity() {
        start<AuthActivity>()
        showToast("Logged out")
        finish()
    }

    override fun setToolBarTitle(title: String) {
        toolbarTitle?.setAnimatedText(title)
    }

    override fun onFragmentChanged(currentTag: String, currentFragment: Fragment) {
        presenter.handleFragmentChanges(currentTag, currentFragment)
    }

    override fun updateDrawerInfo(user: User) {
        val header = navView.header
        with(header) {
            userName.text = user.name
            userInfo.text = user.location
            userAvatar.load(user.avatarUrl, TransformationType.CIRCLE)
        }
    }

    override fun checkNavigationItem(position: Int) {
        navView?.let {
            navView.setChecked(position)
        }
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            else -> super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: NavigationItem) {
        when (item.id) {
            Id.SHOT -> {
                goTo<ShotRootFragment>()
            }
            Id.USER_LIKES -> {
                goTo<UserLikesFragment>()
            }
            Id.FOLLOWING -> {
                goTo<UserFollowingFragment>()
            }
            Id.ABOUT -> {
                goTo<AboutFragment>()
            }
            Id.LOG_OUT -> {
                presenter.logOut()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
    }
}
