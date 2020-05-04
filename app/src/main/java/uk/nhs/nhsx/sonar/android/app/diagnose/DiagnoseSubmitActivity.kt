/*
 * Copyright © 2020 NHSX. All rights reserved.
 */

package uk.nhs.nhsx.sonar.android.app.diagnose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_review_diagnosis.submit_diagnosis
import kotlinx.android.synthetic.main.symptom_banner.toolbar
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import org.joda.time.LocalDate
import timber.log.Timber
import uk.nhs.nhsx.sonar.android.app.BaseActivity
import uk.nhs.nhsx.sonar.android.app.R
import uk.nhs.nhsx.sonar.android.app.appComponent
import uk.nhs.nhsx.sonar.android.app.notifications.Reminders
import uk.nhs.nhsx.sonar.android.app.status.Symptom
import uk.nhs.nhsx.sonar.android.app.status.UserStateFactory
import uk.nhs.nhsx.sonar.android.app.status.UserStateStorage
import uk.nhs.nhsx.sonar.android.app.status.navigateTo
import uk.nhs.nhsx.sonar.android.app.util.NonEmptySet
import javax.inject.Inject

class DiagnoseSubmitActivity : BaseActivity() {
    @Inject
    protected lateinit var userStateStorage: UserStateStorage

    @Inject
    protected lateinit var reminders: Reminders

    private val symptoms: NonEmptySet<Symptom> by lazy {
        @Suppress("UNCHECKED_CAST")
        intent.getSerializableExtra(SYMPTOMS) as NonEmptySet<Symptom>
    }

    private val symptomsDate: LocalDate by lazy {
        DateTime(intent.getLongExtra(SYMPTOMS_DATE, 0), UTC).toLocalDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        setContentView(R.layout.activity_submit_diagnosis)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        submit_diagnosis.setOnClickListener {
            SubmitContactEventsWorker.schedule(this, symptomsDate)
            updateStateAndNavigate()
        }
    }

    override fun handleInversion(inversionModeEnabled: Boolean) {
        if (inversionModeEnabled) {
            submit_diagnosis.setBackgroundResource(R.drawable.button_round_background_inversed)
        } else {
            submit_diagnosis.setBackgroundResource(R.drawable.button_round_background)
        }
    }

    private fun updateStateAndNavigate() {
        val state = UserStateFactory.decide(symptomsDate, symptoms)
        state.scheduleCheckInReminder(reminders)
        userStateStorage.update(state)

        Timber.d("Updated the state to: $state")

        navigateTo(state)
    }

    companion object {

        private const val SYMPTOMS = "SYMPTOMS"
        private const val SYMPTOMS_DATE = "SYMPTOMS_DATE"

        fun start(context: Context, symptoms: NonEmptySet<Symptom>, symptomsDate: DateTime) =
            context.startActivity(getIntent(context, symptoms, symptomsDate))

        private fun getIntent(
            context: Context,
            symptoms: NonEmptySet<Symptom>,
            symptomsDate: DateTime
        ) =
            Intent(context, DiagnoseSubmitActivity::class.java).apply {
                putExtra(SYMPTOMS, symptoms)
                putExtra(SYMPTOMS_DATE, symptomsDate.millis)
            }
    }
}
