package com.techtitans.alleysway

import android.os.Parcel
import android.os.Parcelable

// Implement Parcelable for SetData
data class SetData(
    var reps: Int = 0,
    var weight: Double = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(reps)
        parcel.writeDouble(weight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SetData> {
        override fun createFromParcel(parcel: Parcel): SetData {
            return SetData(parcel)
        }

        override fun newArray(size: Int): Array<SetData?> {
            return arrayOfNulls(size)
        }
    }
}

// Implement Parcelable for ExerciseData
data class ExerciseData(
    val name: String,
    var sets: MutableList<SetData> = mutableListOf(SetData())
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        mutableListOf<SetData>().apply {
            parcel.readList(this, SetData::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeList(sets)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExerciseData> {
        override fun createFromParcel(parcel: Parcel): ExerciseData {
            return ExerciseData(parcel)
        }

        override fun newArray(size: Int): Array<ExerciseData?> {
            return arrayOfNulls(size)
        }
    }
}
data class WorkoutData(
    val name: String,
    val date: String,
    val totalWeight: Double,
    val totalReps: Int,
    val exercises: MutableList<ExerciseData>,
    val timestamp: Long = 0L // Add the timestamp field with a default value
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readInt(),
        mutableListOf<ExerciseData>().apply {
            parcel.readList(this, ExerciseData::class.java.classLoader)
        },
        parcel.readLong() // Read the timestamp from the parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(date)
        parcel.writeDouble(totalWeight)
        parcel.writeInt(totalReps)
        parcel.writeList(exercises)
        parcel.writeLong(timestamp) // Write the timestamp to the parcel
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<WorkoutData> {
        override fun createFromParcel(parcel: Parcel): WorkoutData = WorkoutData(parcel)
        override fun newArray(size: Int): Array<WorkoutData?> = arrayOfNulls(size)
    }
}