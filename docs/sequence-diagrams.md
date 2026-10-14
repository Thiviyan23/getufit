# GetUFit Sequence Diagrams

These diagrams describe the primary application workflows implemented by the Spring MVC controllers and services.

## 1. Member Signup

```mermaid
sequenceDiagram
    actor Member
    participant Browser
    participant JoinController
    participant Validator as JoinForm Validator
    participant Encoder as PasswordEncoder
    participant MemberRepository
    participant Database

    Member->>Browser: Enter signup details
    Browser->>JoinController: GET /join
    JoinController-->>Browser: Render join form
    Member->>Browser: Submit signup form
    Browser->>JoinController: POST /join
    JoinController->>Validator: Validate required fields and formats
    JoinController->>MemberRepository: existsByEmail(email)
    MemberRepository->>Database: Check email
    Database-->>MemberRepository: Exists / does not exist
    MemberRepository-->>JoinController: Email lookup result
    alt Validation errors, duplicate email, or passwords differ
        JoinController-->>Browser: Render form with errors
    else Valid signup
        JoinController->>Encoder: Encode password
        Encoder-->>JoinController: Password hash
        JoinController->>MemberRepository: Save member (status Pending)
        MemberRepository->>Database: Insert member
        Database-->>MemberRepository: Saved member
        MemberRepository-->>JoinController: Saved member
        JoinController-->>Browser: Redirect to /join/success
        Browser-->>Member: Show signup success
    end
```

## 2. Member Login

```mermaid
sequenceDiagram
    actor Member
    participant Browser
    participant AuthController
    participant MemberRepository
    participant Database
    participant Encoder as PasswordEncoder
    participant Session as HTTP Session

    Member->>Browser: Open login page
    Browser->>AuthController: GET /login
    AuthController->>Session: Check for existing memberId
    alt Already signed in
        AuthController-->>Browser: Redirect to /account
    else Not signed in
        AuthController-->>Browser: Render login form
        Member->>Browser: Submit email and password
        Browser->>AuthController: POST /login
        AuthController->>MemberRepository: findByEmail(email)
        MemberRepository->>Database: Find member by email
        Database-->>MemberRepository: Member or no result
        MemberRepository-->>AuthController: Member lookup result
        AuthController->>Encoder: Verify submitted password against stored hash
        Encoder-->>AuthController: Match / no match
        alt Invalid credentials
            AuthController-->>Browser: Render login form with error
        else Password matches, membership Pending or inactive
            AuthController-->>Browser: Render status-specific error
        else Password matches and membership Active
            AuthController->>Session: Invalidate old session, create session with memberId
            AuthController-->>Browser: Redirect to /account
            Browser-->>Member: Show member dashboard
        end
    end
```

## 3. Class Booking and Cancellation

```mermaid
sequenceDiagram
    actor Member
    participant Browser
    participant AuthInterceptor
    participant AccountController
    participant RegistrationService
    participant MemberRepository
    participant GymClassRepository
    participant RegistrationRepository
    participant Database

    Member->>Browser: Open available classes
    Browser->>AuthInterceptor: GET /account/classes
    AuthInterceptor->>Database: Resolve memberId from session
    Database-->>AuthInterceptor: Current member
    AuthInterceptor-->>AccountController: Attach currentMember
    AccountController->>GymClassRepository: Find upcoming classes
    AccountController->>RegistrationRepository: Count registrations and find member bookings
    AccountController-->>Browser: Render classes, remaining seats, and booked status
    Member->>Browser: Select a class to book
    Browser->>AuthInterceptor: POST /account/classes/register
    AuthInterceptor-->>AccountController: Attach currentMember
    AccountController->>RegistrationService: register(memberId, classId)
    RegistrationService->>MemberRepository: Find member
    RegistrationService->>GymClassRepository: Find class
    RegistrationService->>RegistrationRepository: Check existing registration and capacity
    RegistrationRepository->>Database: Query registration records
    Database-->>RegistrationRepository: Existing booking / registered count
    RegistrationRepository-->>RegistrationService: Registration checks
    alt Member inactive, class/member missing, already booked, or class full
        RegistrationService-->>AccountController: Return error
        AccountController-->>Browser: Redirect to classes with error message
    else Booking allowed
        RegistrationService->>RegistrationRepository: Save Registered registration
        RegistrationRepository->>Database: Insert registration
        Database-->>RegistrationRepository: Saved registration
        RegistrationRepository-->>RegistrationService: Registration
        RegistrationService-->>AccountController: Registration
        AccountController-->>Browser: Redirect to classes with success message
    end
    opt Member cancels a booking
        Member->>Browser: Cancel registration
        Browser->>AuthInterceptor: POST /account/registrations/cancel
        AuthInterceptor-->>AccountController: Attach currentMember
        AccountController->>RegistrationService: List member registrations
        RegistrationService->>RegistrationRepository: Find registrations for member
        RegistrationRepository-->>RegistrationService: Member registrations
        RegistrationService-->>AccountController: Member registrations
        alt Registration is not owned by member
            AccountController-->>Browser: Redirect with not-found error
        else Registration belongs to member
            AccountController->>RegistrationService: cancel(registrationId)
            RegistrationService->>RegistrationRepository: Find registration and set status Cancelled
            RegistrationRepository->>Database: Update registration
            Database-->>RegistrationRepository: Updated registration
            RegistrationService-->>AccountController: Cancelled registration
            AccountController-->>Browser: Redirect to account with success message
        end
    end
```

## 4. Staff Class Scheduling

```mermaid
sequenceDiagram
    actor Staff
    participant Browser
    participant ScheduleController
    participant TrainerRepository
    participant GymClassRepository
    participant RegistrationRepository
    participant Database

    Staff->>Browser: Open class schedule form
    Browser->>ScheduleController: GET /schedule/new
    ScheduleController->>TrainerRepository: Load trainers for form
    TrainerRepository->>Database: Find trainers
    Database-->>TrainerRepository: Trainer list
    TrainerRepository-->>ScheduleController: Trainer list
    ScheduleController-->>Browser: Render form with default capacity
    Staff->>Browser: Submit class details
    Browser->>ScheduleController: POST /schedule/new
    ScheduleController->>ScheduleController: Validate fields, date/time, trainer status, and capacity
    ScheduleController->>TrainerRepository: Find selected trainer
    TrainerRepository-->>ScheduleController: Trainer status
    ScheduleController->>GymClassRepository: Find trainer classes on selected date
    GymClassRepository->>Database: Query trainer schedule
    Database-->>GymClassRepository: Classes on date
    GymClassRepository-->>ScheduleController: Existing classes
    alt Invalid fields, inactive/missing trainer, or overlapping class
        ScheduleController-->>Browser: Re-render form with validation errors
    else Valid schedule
        ScheduleController->>GymClassRepository: Save class
        GymClassRepository->>Database: Insert class
        Database-->>GymClassRepository: Saved class
        GymClassRepository-->>ScheduleController: Saved class
        ScheduleController-->>Browser: Redirect to /schedule with success message
    end
```
