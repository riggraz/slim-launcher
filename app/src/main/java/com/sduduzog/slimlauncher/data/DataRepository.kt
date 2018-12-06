package com.sduduzog.slimlauncher.data

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import java.util.*

class DataRepository(application: Application) {

    private val db: DataRoomDatabase = DataRoomDatabase.getDatabase(application)!!
    private val appDao: AppDao = db.appDao()
    private val noteDao: NoteDao = db.noteDao()
    private var _apps: LiveData<List<App>> = appDao.apps
    private var _homeApps: LiveData<List<HomeApp>> = appDao.homeApps
    private var _notes: LiveData<List<Note>> = noteDao.notes

    private var pm: PackageManager = application.packageManager

    val homeApps: LiveData<List<HomeApp>>
        get() = _homeApps

    val apps: LiveData<List<App>>
        get() = _apps

    val notes: LiveData<List<Note>>
    get() = _notes

    fun insertHomeApp(app: HomeApp) {
        InsertHomeAppAsyncTask(appDao).execute(app)
    }

    fun deleteHomeApp(app: HomeApp) {
        DeleteHomeAppAsyncTask(appDao).execute(app)
    }

    fun updateApps(list: List<HomeApp>) {
        for (app in list) {
            UpdateAppsAsyncTask(appDao).execute(app)
        }
    }

    fun refreshApps() {
        RefreshAppsAsyncTask(appDao).execute(pm)
    }

    fun saveNote(note: Note){
        SaveNoteAsyncTask(noteDao).execute(note)
    }

    fun updateNote(note: Note){
        UpdateNoteAsyncTask(noteDao).execute(note)
    }

    fun deleteNote(note: Note){
        DeleteNoteAsyncTask(noteDao).execute(note)
    }

    private class InsertHomeAppAsyncTask internal constructor(private val mAsyncTaskDao: AppDao) : AsyncTask<HomeApp, Void, Void>() {

        override fun doInBackground(vararg params: HomeApp): Void? {
            mAsyncTaskDao.addHomeApp(params[0])
            return null
        }
    }

    private class DeleteHomeAppAsyncTask internal constructor(private val mAsyncTaskDao: AppDao) : AsyncTask<HomeApp, Void, Void>() {

        override fun doInBackground(vararg params: HomeApp): Void? {
            mAsyncTaskDao.delete(params[0])
            return null
        }
    }

    private class UpdateAppsAsyncTask internal constructor(private val mAsyncTaskDao: AppDao) : AsyncTask<HomeApp, Void, Void>() {

        override fun doInBackground(vararg params: HomeApp): Void? {
            mAsyncTaskDao.updateHomeApp(params[0])
            return null
        }
    }

    private class RefreshAppsAsyncTask internal constructor(private val mAsyncTaskDao: AppDao) : AsyncTask<PackageManager, Void, Void>() {

        @Suppress("PropertyName")
        private val _tag = "DataRepository"

        override fun doInBackground(vararg params: PackageManager): Void? {
            val pm = params[0]
            val main = Intent(Intent.ACTION_MAIN, null)

            main.addCategory(Intent.CATEGORY_LAUNCHER)

            val launchables = pm.queryIntentActivities(main, 0)
            Collections.sort(launchables,
                    ResolveInfo.DisplayNameComparator(pm))
            for (i in launchables.indices) {
                val item = launchables[i]
                Log.d(_tag, "$item")
                val activity = item.activityInfo
                val app = App(launchables[i].loadLabel(pm).toString(), activity.applicationInfo.packageName, activity.name)
                mAsyncTaskDao.insert(app)
            }
            return null
        }
    }

    private class SaveNoteAsyncTask internal constructor(private val mAsyncTaskDao: NoteDao) : AsyncTask<Note, Void, Void>() {

        override fun doInBackground(vararg params: Note): Void? {
            val note = params[0]
            mAsyncTaskDao.saveNote(note)
            return null
        }
    }

    private class UpdateNoteAsyncTask internal constructor(private val mAsyncTaskDao: NoteDao) : AsyncTask<Note, Void, Void>() {

        override fun doInBackground(vararg params: Note): Void? {
            val note = params[0]
            mAsyncTaskDao.updateNote(note)
            return null
        }
    }

    private class DeleteNoteAsyncTask internal constructor(private val mAsyncTaskDao: NoteDao) : AsyncTask<Note, Void, Void>() {

        override fun doInBackground(vararg params: Note): Void? {
            val note = params[0]
            mAsyncTaskDao.deleteNote(note)
            return null
        }
    }


    companion object {

        @Volatile
        @JvmStatic
        private var INSTANCE: DataRepository? = null

        fun getInstance(application: Application): DataRepository {
            synchronized(DataRepository::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = DataRepository(application)
                }
                return INSTANCE!!
            }
        }
    }
}