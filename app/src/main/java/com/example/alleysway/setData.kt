package com.example.alleysway

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
