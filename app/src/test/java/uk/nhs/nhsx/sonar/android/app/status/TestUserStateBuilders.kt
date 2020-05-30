package uk.nhs.nhsx.sonar.android.app.status

import org.joda.time.DateTime
import org.joda.time.LocalDate
import uk.nhs.nhsx.sonar.android.app.util.NonEmptySet
import uk.nhs.nhsx.sonar.android.app.util.nonEmptySetOf
import uk.nhs.nhsx.sonar.android.app.util.toUtcNormalized

fun buildExposedState(
    since: DateTime = LocalDate.now().toUtcNormalized(),
    until: DateTime = LocalDate.now().plusDays(14).toUtcNormalized()
) = ExposedState(since, until)

fun buildSymptomaticState(
    since: DateTime = LocalDate.now().toUtcNormalized(),
    until: DateTime = LocalDate.now().plusDays(7).toUtcNormalized(),
    symptoms: NonEmptySet<Symptom> = nonEmptySetOf(Symptom.COUGH)
) = SymptomaticState(since, until, symptoms)

fun buildExposedSymptomaticState(
    since: DateTime = LocalDate.now().toUtcNormalized(),
    until: DateTime = LocalDate.now().plusDays(7).toUtcNormalized(),
    symptoms: NonEmptySet<Symptom> = nonEmptySetOf(Symptom.COUGH)
) = ExposedSymptomaticState(since, until, symptoms)

fun buildPositiveState(
    since: DateTime = LocalDate.now().toUtcNormalized(),
    until: DateTime = LocalDate.now().plusDays(7).toUtcNormalized(),
    symptoms: NonEmptySet<Symptom> = nonEmptySetOf(Symptom.COUGH)
) = PositiveState(since, until, symptoms)
