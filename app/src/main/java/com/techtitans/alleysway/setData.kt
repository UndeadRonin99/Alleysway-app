package com.techtitans.alleysway

// Import statements for Parcel and Parcelable
import android.os.Parcel
import android.os.Parcelable

// Data class SetData with Parcelable implementation to handle sets within an exercise
data class SetData(
    var reps: Int = 0, // Number of repetitions for a set
    var weight: Double = 0.0 // Weight used in a set
) : Parcelable {
    // Constructor to create SetData from a Parcel
    constructor(parcel: Parcel) : this(
        parcel.readInt(), // Read reps from the parcel
        parcel.readDouble() // Read weight from the parcel
    )

    // Method to write SetData to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(reps) // Write reps to the parcel
        parcel.writeDouble(weight) // Write weight to the parcel
    }

    // Required method for Parcelable with no specific content description
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to generate instances of this Parcelable class
    companion object CREATOR : Parcelable.Creator<SetData> {
        override fun createFromParcel(parcel: Parcel): SetData = SetData(parcel)
        override fun newArray(size: Int): Array<SetData?> = arrayOfNulls(size)
    }
}

// Data class ExerciseData with Parcelable implementation to handle exercises
data class ExerciseData(
    val name: String, // Name of the exercise
    var sets: MutableList<SetData> = mutableListOf(SetData()) // List of sets in the exercise
) : Parcelable {
    // Constructor to create ExerciseData from a Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Read name from the parcel, provide default if null
        mutableListOf<SetData>().apply {
            parcel.readList(this, SetData::class.java.classLoader) // Read the list of sets from the parcel
        }
    )

    // Method to write ExerciseData to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name) // Write the name to the parcel
        parcel.writeList(sets) // Write the list of sets to the parcel
    }

    // Required method for Parcelable with no specific content description
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to generate instances of this Parcelable class
    companion object CREATOR : Parcelable.Creator<ExerciseData> {
        override fun createFromParcel(parcel: Parcel): ExerciseData = ExerciseData(parcel)
        override fun newArray(size: Int): Array<ExerciseData?> = arrayOfNulls(size)
    }
}

// Data class WorkoutData with Parcelable implementation to handle workout sessions
data class WorkoutData(
    val name: String, // Name of the workout
    val date: String, // Date of the workout
    val totalWeight: Double, // Total weight lifted during the workout
    val totalReps: Int, // Total repetitions performed during the workout
    val exercises: MutableList<ExerciseData>, // List of exercises in the workout
    val timestamp: Long = 0L // Timestamp of the workout session for tracking (default value provided)
) : Parcelable {
    // Constructor to create WorkoutData from a Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Read workout name, provide default if null
        parcel.readString() ?: "", // Read workout date, provide default if null
        parcel.readDouble(), // Read total weight from the parcel
        parcel.readInt(), // Read total reps from the parcel
        mutableListOf<ExerciseData>().apply {
            parcel.readList(this, ExerciseData::class.java.classLoader) // Read the list of exercises from the parcel
        },
        parcel.readLong() // Read the timestamp from the parcel
    )

    // Method to write WorkoutData to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name) // Write the workout name to the parcel
        parcel.writeString(date) // Write the workout date to the parcel
        parcel.writeDouble(totalWeight) // Write the total weight to the parcel
        parcel.writeInt(totalReps) // Write the total reps to the parcel
        parcel.writeList(exercises) // Write the list of exercises to the parcel
        parcel.writeLong(timestamp) // Write the timestamp to the parcel
    }

    // Required method for Parcelable with no specific content description
    override fun describeContents(): Int = 0

    // Companion object to generate instances of this Parcelable class
    companion object CREATOR : Parcelable.Creator<WorkoutData> {
        override fun createFromParcel(parcel: Parcel): WorkoutData = WorkoutData(parcel)
        override fun newArray(size: Int): Array<WorkoutData?> = arrayOfNulls(size)
    }
}
