package com.riccardograziosi.slimlauncher.di

import com.riccardograziosi.slimlauncher.data.BaseDao
import com.riccardograziosi.slimlauncher.models.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class RepositoryModule {
    @Provides
    @ViewModelScoped
    fun providesRepository(baseDao: BaseDao): Repository {
        return Repository(baseDao)
    }
}