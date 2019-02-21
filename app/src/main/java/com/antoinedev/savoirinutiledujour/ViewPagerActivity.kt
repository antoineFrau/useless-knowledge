package com.antoinedev.savoirinutiledujour

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.antoinedev.savoirinutiledujour.UtilsThings.ConnectivityReceiver
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener

import kotlinx.android.synthetic.main.activity_view_pager.*
import kotlinx.android.synthetic.main.fragment_view_pager.*
import kotlinx.android.synthetic.main.fragment_view_pager.view.*
import android.support.design.widget.TabLayout
import com.antoinedev.savoirinutiledujour.UtilsThings.isHashcodeEquals
import java.util.*



class ViewPagerActivity : AppCompatActivity(), DataListener, InternetConnectivityListener {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mSnackBar: Snackbar? = null
    private var knowledgeItemList: ArrayList<KnowledgeItem>? = null
    private var timeClickRefresh: Date? = null
    private var timeClickRefreshReactivate: Date? = null

    private lateinit var mInternetAvailabilityChecker: InternetAvailabilityChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)
        toolbar.title = "Les savoirs de la semaine"
        setSupportActionBar(toolbar)

        MainController.getInstance(applicationContext).getData(this, isNetworkAvailable())

        // Listen Internet status event
        InternetAvailabilityChecker.init(this)
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        mInternetAvailabilityChecker.addInternetConnectivityListener(this)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    override fun onInternetConnectivityChanged(isConnected: Boolean) {
        if (isConnected) {
            if(mSectionsPagerAdapter != null && this.knowledgeItemList == null)
                showSnackBar("Vous êtes maintenant en ligne vous pouvez rafrechir !")
            else
                showSnackBar("Vous êtes maintenant en ligne !")
            // Think about add -> MainController.getInstance(applicationContext).getData(this, isNetworkAvailable()) -> Here
        } else showSnackBar("Mode hors ligne activié..", 4000)
    }

    override fun notifyRetrieved(knowledgeItems: ArrayList<KnowledgeItem>) {
        knowledgeItems.sortBy { item -> item.id }

        if (this.knowledgeItemList != null && this.knowledgeItemList!!.isHashcodeEquals(knowledgeItems)) {
            showSnackBar("Pas de nouvelle données.",2000)
            return
        }

        this.knowledgeItemList = knowledgeItems

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, this.knowledgeItemList!!)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        tabDots.setupWithViewPager(container)
    }

    override fun notifyNotRetrieved() {
        if (isNetworkAvailable()) {
            showSnackBar("Aucune donnée trouvée ! Essayez à nouveau !", 2000)
        } else showSnackBar("Mode hors ligne activié..", 4000)
    }

    fun showSnackBar(content: String, duration: Int = Snackbar.LENGTH_INDEFINITE) {
        val messageToUser = content
        mSnackBar = Snackbar.make(findViewById(R.id.main_content), messageToUser, Snackbar.LENGTH_LONG)
        mSnackBar?.duration = duration
        mSnackBar?.show()
    }

    fun hideSnackBar() {
        mSnackBar?.dismiss()
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_view_pager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_refresh) {
            if (!isNetworkAvailable()) {
                showSnackBar("Veuillez vous connecter à internet pour apprendre plus de savoir !",4000)
                return true
            }
            // We are blocking user to refresh he already refresh 3seconds ago.
            timeClickRefresh = Date()
            if(timeClickRefresh != null && timeClickRefreshReactivate != null
                    && timeClickRefresh?.time!! <= timeClickRefreshReactivate?.time!!) {
                Log.d("ViewPagerTest", "HERE_REFRESH")
                return true
            }
            val cal = Calendar.getInstance()
            cal.time = timeClickRefresh
            cal.add(Calendar.SECOND, 3)

            timeClickRefreshReactivate = cal.time

            MainController.getInstance(applicationContext).getData(this, isNetworkAvailable())
            hideSnackBar()
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager, var listKnowledgeItems: ArrayList<KnowledgeItem>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(listKnowledgeItems.get(position))
        }

        override fun getCount(): Int {
            // Show 5 total pages.
            return this.listKnowledgeItems.size
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_view_pager, container, false)
            rootView.item_id.text = getString(R.string.section_format, arguments?.getInt(ARG_SECTION_ID))
            rootView.title.text = arguments?.getString(ARG_SECTION_TITLE)
            rootView.description.text = arguments?.getString(ARG_SECTION_DESCRIPTION)
            rootView.date.text = arguments?.getString(ARG_SECTION_DATE)
            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_ID = "section_id"
            private const val ARG_SECTION_TITLE = "section_title"
            private const val ARG_SECTION_DESCRIPTION = "section_description"
            private const val ARG_SECTION_DATE = "section_date"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(item: KnowledgeItem): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_ID, item.id)
                args.putString(ARG_SECTION_TITLE, item.title)
                args.putString(ARG_SECTION_DESCRIPTION, item.description)
                args.putString(ARG_SECTION_DATE, item.date)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
