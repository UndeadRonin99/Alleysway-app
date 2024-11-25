# Alleysway App

Alleysway is a comprehensive fitness application designed to enhance your workout experience, track your progress, and connect you with personal trainers. The app offers a range of features, including workout logging, weight tracking, booking training sessions, viewing exercises, and participating in leaderboards. Built for Android devices, Alleysway aims to provide users with an engaging and interactive platform to achieve their fitness goals.

- [Video for app and website](https://youtu.be/OxSpskRw8iA)

## Table of Contents

- [Features](#features)
  - [Workout Logging](#workout-logging)
  - [Weight Tracking](#weight-tracking)
  - [Booking Training Sessions](#booking-training-sessions)
  - [Exercise Library](#exercise-library)
  - [Leaderboard](#leaderboard)
  - [Attendance Tracking](#attendance-tracking)
- [Installation](#installation)
- [Usage](#usage)
  - [Navigating the App](#navigating-the-app)
  - [Logging a Workout](#logging-a-workout)
  - [Tracking Weight](#tracking-weight)
  - [Booking a Session](#booking-a-session)
  - [Viewing Exercises](#viewing-exercises)
  - [Participating in the Leaderboard](#participating-in-the-leaderboard)
- [Code Structure](#code-structure)
  - [Main Components](#main-components)
  - [Data Models](#data-models)
  - [Adapters](#adapters)
  - [Firebase Integration](#firebase-integration)
  - [Unit Testing](#unit-testing)


## Features

### Workout Logging

- **Select Exercises**: Choose from a vast library of exercises grouped by muscle groups.
- **Log Sets and Reps**: Record the number of sets, reps, and weight lifted for each exercise.
- **View Past Workouts**: Access your workout history to track progress over time.

### Weight Tracking

- **Add Weight Entries**: Log your weight with corresponding dates.
- **Set Weight Goals**: Define target weight goals and monitor your progress towards them.
- **Visualize Progress**: View your weight trends over time with interactive graphs.
- **Predict Future Weight**: Utilize linear regression and exponential smoothing to forecast future weight trends.

### Booking Training Sessions

- **Find Trainers**: Browse through available personal trainers.
- **Schedule Sessions**: Book training sessions based on trainer availability.
- **Payment Integration**: Securely pay for sessions through the app.
- **Notifications**: Receive reminders for upcoming sessions.

### Exercise Library

- **Detailed Exercise Information**: Access descriptions, tips, and images for each exercise.
- **Search Functionality**: Quickly find exercises using the search feature.
- **Favorites**: Mark exercises as favorites for easy access.

### Leaderboard

- **Global Ranking**: Compete with other users based on total weight lifted.
- **Top Performers**: View the top three users on the home screen.
- **Opt-In Privacy**: Choose whether to participate in the leaderboard.

### Attendance Tracking

- **QR Code Scanning**: Scan QR codes to mark attendance at the gym.
- **Attendance Calendar**: Visualize your gym attendance over time.
- **Public Attendance Data**: Contribute to overall attendance statistics.

## Installation

To run the Alleysway app locally, follow these steps:

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/yourusername/alleysway.git

## Open in Android Studio:

- Launch Android Studio.
- Click on **File > Open**.
- Navigate to the cloned repository and select it.

## Set Up Firebase:

- Create a new project in [Firebase Console](https://console.firebase.google.com/).
- Add an Android app to your Firebase project.
- Download the `google-services.json` file.
- Place the `google-services.json` file in the `app/` directory of your project.

## Configure Dependencies:

- Ensure all Gradle dependencies are up to date.
- Sync the project with Gradle files.

## Run the App:

- Connect your Android device or start an emulator.
- Click the **Run** button in Android Studio.

## Usage

### Navigating the App

- **Home Screen**: Access key features such as workout logging, weight tracking, and booking.
- **Bottom Navigation Bar**: Quickly switch between main sections like Home, Workouts, Tracker, Booking, and QR Scanner.

### Logging a Workout

#### Access Workout Logging:

- Navigate to the **Workouts** section.
- Tap on **Log a Workout**.

#### Select Exercises:

- Choose exercises from different muscle groups.
- Use the search bar to find specific exercises.

#### Log Details:

- For each exercise, add sets with the number of reps and weight lifted.
- Save the workout once completed.

### Tracking Weight

#### Add Weight Entry:

- Navigate to the **Tracker** section.
- Tap on **Add Data**.
- Enter your current weight and select the date.

#### Set Weight Goal:

- Tap on **Set Goal**.
- Enter your target weight.

#### View Progress:

- Use the **Statistics** button to view your weight trends.
- Select date ranges to customize the displayed data.

### Booking a Session

#### Find a Trainer:

- Navigate to the **Bookings** section.
- Browse available trainers with their rates and profiles.

#### Select Time Slots:

- Choose preferred dates and times based on trainer availability.
- Add selected time slots to your booking.

#### Finalize Booking:

- Review your booking details.
- Proceed to payment if required.

### Viewing Exercises

#### Access Exercise Library:

- Navigate to the **Exercises** section.

#### Browse and Search:

- Explore exercises grouped by muscle groups.
- Use the search bar to find specific exercises.

#### View Details:

- Tap on an exercise to view detailed information, tips, and images.

### Participating in the Leaderboard

#### Opt-In:

- Ensure you have opted in to participate in the leaderboard via settings.

#### View Rankings:

- Navigate to the **Leaderboard** section.
- See your ranking and compare with others.

#### Improve Ranking:

- Log workouts regularly to increase your total weight lifted.

## Code Structure

### Main Components

- **Activities**: Each major screen in the app is represented by an activity (e.g., `Tracker`, `Workouts`, `Bookings`).
- **Adapters**: Used for managing data in lists and views (e.g., `WorkoutAdapter`, `SetAdapter`).
- **Data Models**: Classes representing data structures (e.g., `Exercise`, `WorkoutData`, `LeaderboardEntry`).

### Data Models

- **Exercise**: Represents an exercise with attributes like name, main muscle group, tips, and image URL.
- **SetData**: Represents a set within an exercise, including reps and weight.
- **WorkoutData**: Contains information about a workout session, including exercises and totals.
- **LeaderboardEntry**: Holds data for a user's leaderboard ranking.

### Adapters

- **WorkoutAdapter**: Manages the display of exercises and their sets within a workout.
- **SetAdapter**: Handles individual sets within an exercise.
- **CalendarAdapter**: Displays attendance data in a calendar view.
- **SelectableExerciseAdapter**: Used for selecting exercises when logging a workout.

### Firebase Integration

- **Authentication**: User accounts are managed via Firebase Authentication.
- **Database**: Firebase Realtime Database stores user data, workouts, exercises, and bookings.
- **Storage**: Profile images and exercise images are stored in Firebase Storage.

### Unit Testing

- **FinalizeBookingsTest**: Contains unit tests for booking logic, ensuring time slot calculations and availability checks function correctly.

