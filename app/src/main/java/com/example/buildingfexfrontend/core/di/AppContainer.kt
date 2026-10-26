package com.example.buildingfexfrontend.core.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.buildingfexfrontend.BuildConfig
import com.example.buildingfexfrontend.core.data.network.NetworkModule
import com.example.buildingfexfrontend.core.data.session.SessionRepositoryImpl
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.finances.application.CollectionsUseCases
import com.example.buildingfexfrontend.finances.application.FinanceUseCases
import com.example.buildingfexfrontend.finances.data.repository.CollectionsRepositoryImpl
import com.example.buildingfexfrontend.finances.data.repository.FinancesRepositoryImpl
import com.example.buildingfexfrontend.iam.application.AuthUseCases
import com.example.buildingfexfrontend.iam.data.repository.AuthRepositoryImpl
import com.example.buildingfexfrontend.imports.application.ImportsUseCases
import com.example.buildingfexfrontend.imports.data.repository.ImportsRepositoryImpl
import com.example.buildingfexfrontend.incidents.application.IncidentsUseCases
import com.example.buildingfexfrontend.incidents.data.repository.IncidentsRepositoryImpl
import com.example.buildingfexfrontend.information.application.AnnouncementsUseCases
import com.example.buildingfexfrontend.information.data.repository.AnnouncementsRepositoryImpl
import com.example.buildingfexfrontend.residents.application.ResidentsUseCases
import com.example.buildingfexfrontend.residents.data.repository.ResidentsRepositoryImpl
import com.example.buildingfexfrontend.socialspaces.application.ReservationsUseCases
import com.example.buildingfexfrontend.socialspaces.application.SpacesUseCases
import com.example.buildingfexfrontend.socialspaces.data.repository.ReservationsRepositoryImpl
import com.example.buildingfexfrontend.socialspaces.data.repository.SpacesRepositoryImpl
import com.example.buildingfexfrontend.subscription.application.SubscriptionUseCases
import com.example.buildingfexfrontend.subscription.data.repository.SubscriptionRepositoryImpl
import com.example.buildingfexfrontend.support.application.SupportUseCases
import com.example.buildingfexfrontend.support.data.repository.SupportRepositoryImpl
import com.example.buildingfexfrontend.team.application.TeamUseCases
import com.example.buildingfexfrontend.team.data.repository.TeamRepositoryImpl

/**
 * Composition root (manual DI): wires every bounded context together.
 * The presentation layer only talks to this container, never to Retrofit.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val session: SessionRepository = SessionRepositoryImpl(appContext)

    val network: NetworkModule = NetworkModule(
        baseUrl = BuildConfig.API_BASE_URL,
        session = session,
        enableLogs = BuildConfig.DEBUG,
    )

    val auth: AuthUseCases by lazy {
        AuthUseCases(
            repository = AuthRepositoryImpl(network.service(), session),
            session = session,
        )
    }

    val residents: ResidentsUseCases by lazy {
        ResidentsUseCases(
            repository = ResidentsRepositoryImpl(network.service(), session),
            session = session,
            subscription = subscription,
        )
    }

    val finances: FinanceUseCases by lazy {
        FinanceUseCases(
            repository = FinancesRepositoryImpl(network.service(), session),
            session = session,
        )
    }

    val collections: CollectionsUseCases by lazy {
        CollectionsUseCases(
            repository = CollectionsRepositoryImpl(network.service(), session),
            session = session,
        )
    }

    val incidents: IncidentsUseCases by lazy {
        IncidentsUseCases(IncidentsRepositoryImpl(network.service(), session))
    }

    val announcements: AnnouncementsUseCases by lazy {
        AnnouncementsUseCases(AnnouncementsRepositoryImpl(network.service(), session), session)
    }

    val spaces: SpacesUseCases by lazy {
        SpacesUseCases(
            spaces = SpacesRepositoryImpl(network.service(), session),
            reservations = ReservationsRepositoryImpl(network.service(), session),
            session = session,
        )
    }

    val reservations: ReservationsUseCases by lazy {
        ReservationsUseCases(ReservationsRepositoryImpl(network.service(), session))
    }

    val support: SupportUseCases by lazy {
        SupportUseCases(SupportRepositoryImpl(network.service(), session), session)
    }

    val team: TeamUseCases by lazy {
        TeamUseCases(TeamRepositoryImpl(network.service(), session), session)
    }

    val imports: ImportsUseCases by lazy {
        ImportsUseCases(ImportsRepositoryImpl(network.service(), session), session)
    }

    val subscription: SubscriptionUseCases by lazy {
        SubscriptionUseCases(SubscriptionRepositoryImpl(network.service(), session), session)
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}
