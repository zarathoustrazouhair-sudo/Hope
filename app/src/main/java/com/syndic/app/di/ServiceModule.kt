package com.syndic.app.di

import com.syndic.app.data.service.PdfServiceImpl
import com.syndic.app.domain.service.PdfService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindPdfService(
        pdfServiceImpl: PdfServiceImpl
    ): PdfService
}
