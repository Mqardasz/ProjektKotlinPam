# Aplikacja Sensorów - Projekt PAM

## Opis projektu

Aplikacja mobilna na Android wykorzystująca sensory urządzenia do zbierania, przetwarzania i prezentacji danych użytkownikowi.

## Implementacja

# Szczegóły Implementacji

## Pełna struktura plików

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/myapplication/
│   ├── MainActivity.kt                    # Punkt wejścia aplikacji
│   ├── data/
│   │   ├── AppDatabase.kt                 # Konfiguracja Room Database
│   │   ├── SensorDao.kt                   # Interfejs dostępu do danych
│   │   ├── SensorMeasurement.kt           # Model danych (Entity)
│   │   └── SensorRepository.kt            # Warstwa abstrakcji repozytorium
│   ├── sensors/
│   │   ├── LocationManager.kt             # Manager lokalizacji GPS
│   │   └── AccelerometerManager.kt        # Manager akcelerometru
│   ├── ui/
│   │   ├── DashboardScreen.kt             # Ekran główny z pomiarami
│   │   ├── DashboardViewModel.kt          # ViewModel dla Dashboard
│   │   ├── HistoryScreen.kt               # Ekran historii pomiarów
│   │   ├── HistoryViewModel.kt            # ViewModel dla historii
│   │   └── theme/
│   │       ├── Color.kt                   # Definicje kolorów
│   │       ├── Theme.kt                   # Główny motyw aplikacji
│   │       └── Type.kt                    # Typografia
│   └── navigation/
│       ├── Routes.kt                      # Type-safe route definitions
│       └── AppNavigation.kt               # Konfiguracja nawigacji
└── res/
    └── values/
        └── strings.xml                    # Zasoby tekstowe
```

## Przepływ danych w aplikacji

```
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity                            │
│  - Inicjalizacja Room Database                              │
│  - Tworzenie Repository                                     │
│  - Uruchomienie Navigation Compose                          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    AppNavigation                            │
│  - NavHost z type-safe routes                               │
│  - Routing między ekranami                                  │
└────────┬──────────────────────────┬─────────────────────────┘
         │                          │
         ▼                          ▼
┌──────────────────┐       ┌───────────────────┐
│ DashboardScreen  │       │  HistoryScreen    │
│  - Live sensors  │       │  - All records    │
│  - Stats cards   │       │  - Filters        │
│  - Save button   │       │  - Delete actions │
└────────┬─────────┘       └─────────┬─────────┘
         │                           │
         ▼                           ▼
┌──────────────────┐       ┌───────────────────┐
│DashboardViewModel│       │ HistoryViewModel  │
│  - Location flow │       │  - Filter state   │
│  - Accel. flow   │       │  - All data flow  │
│  - Save methods  │       │  - Delete methods │
└────────┬─────────┘       └─────────┬─────────┘
         │                           │
         └───────────┬───────────────┘
                     ▼
         ┌─────────────────────┐
         │  SensorRepository   │
         │  - Flow operations  │
         │  - CRUD methods     │
         └──────────┬──────────┘
                    │
         ┌──────────┴──────────┐
         │                     │
         ▼                     ▼
┌─────────────┐       ┌──────────────┐
│  SensorDao  │       │   Sensors    │
│  (Room)     │       │  - Location  │
│  - Queries  │       │  - Accel.    │
│  - Insert   │       └──────────────┘
│  - Delete   │
└─────┬───────┘
      │
      ▼
┌────────────────┐
│  SQLite DB     │
│  - Persistent  │
│    storage     │
└────────────────┘
```

## Szczegółowe funkcje ekranów

### Dashboard Screen

#### Sekcja Statystyk
- **3 karty statystyk**: GPS, Akcelerometr, Razem
- **Liczniki w czasie rzeczywistym**: Aktualizowane automatycznie

#### Sekcja GPS
- Wymaga uprawnień lokalizacji
- Wyświetla aktualną szerokość i długość geograficzną
- Przyciski:
  - **Start/Stop**: Włącza/wyłącza śledzenie lokalizacji
  - **Zapisz**: Zapisuje aktualną pozycję do bazy

#### Sekcja Akcelerometr
- Wyświetla przyśpieszenie w trzech osiach (X, Y, Z) w m/s²
- Przyciski:
  - **Start/Stop**: Włącza/wyłącza odczyt akcelerometru
  - **Zapisz**: Zapisuje aktualne wartości do bazy

#### Sekcja Ostatnie Pomiary
- Lista 5 najnowszych pomiarów
- Każdy pomiar z przyciskiem usuwania
- Przycisk "Usuń wszystko" u góry

### History Screen

#### Filtry
- **Wszystkie**: Pokazuje wszystkie pomiary
- **GPS**: Tylko pomiary GPS
- **Akcelerometr**: Tylko pomiary akcelerometru

#### Lista Pomiarów
- Pełna lista chronologicznie posortowana
- Szczegółowe karty z:
  - Ikona typu sensora
  - Nazwa sensora
  - Data i godzina pomiaru
  - Wartości specyficzne dla sensora:
    - GPS: Szerokość i długość (6 miejsc po przecinku)
    - Akcelerometr: X, Y, Z, Magnituda (3 miejsca po przecinku)
  - Przycisk usuwania dla każdego pomiaru

#### Stan Pusty
- Ikona skrzynki odbiorczej
- Tekst "Brak pomiarów"

## Zarządzanie stanem

### StateFlow
```kotlin
// W ViewModel
val measurements: StateFlow<List<SensorMeasurement>>
val gpsCount: StateFlow<Int>
val accelerometerCount: StateFlow<Int>

// W Composable
val measurements by viewModel.measurements.collectAsState()
```

### Flow dla Sensorów
```kotlin
// Ciągły strumień danych lokalizacji
fun getLocationUpdates(): Flow<Location>

// Ciągły strumień danych akcelerometru
fun getAccelerometerUpdates(): Flow<AccelerometerData>
```

## Wymagania techniczne

### Minimalna wersja Android
- `minSdk = 33` (Android 13)
- `targetSdk = 36` (najnowszy)
- `compileSdk = 36`

### Wymagane sensory
- GPS/Lokalizacja: Wymagany dla pełnej funkcjonalności
- Akcelerometr: Wymagany dla pełnej funkcjonalności
- Oba sensory są sprawdzane runtime i aplikacja obsługuje ich brak

## Architektura

### Warstwa danych (`data`)
- `SensorMeasurement.kt`: Encja Room
- `SensorDao.kt`: Data Access Object
- `AppDatabase.kt`: Konfiguracja bazy danych
- `SensorRepository.kt`: Warstwa abstrakcji dostępu do danych

### Warstwa sensorów (`sensors`)
- `LocationManager.kt`: Obsługa GPS/lokalizacji
- `AccelerometerManager.kt`: Obsługa akcelerometru

### Warstwa UI (`ui`)
- `DashboardScreen.kt`: Główny ekran z aktywnym zbieraniem danych
- `DashboardViewModel.kt`: ViewModel dla Dashboard
- `HistoryScreen.kt`: Ekran historii pomiarów
- `HistoryViewModel.kt`: ViewModel dla historii
- `theme/`: Composable theme (Color, Type, Theme)

### Nawigacja (`navigation`)
- `Routes.kt`: Type-safe routes z kotlinx.serialization
- `AppNavigation.kt`: Konfiguracja NavHost


## Uprawnienia

Aplikacja wymaga następujących uprawnień (zdefiniowanych w `AndroidManifest.xml`):
- `ACCESS_FINE_LOCATION`: Dostęp do precyzyjnej lokalizacji
- `ACCESS_COARSE_LOCATION`: Dostęp do przybliżonej lokalizacji
- `INTERNET`: Dostęp do sieci (dla przyszłych rozszerzeń)
- `CAMERA`: Dostęp do aparatu (przygotowane dla przyszłych rozszerzeń)

## Struktura bazy danych

### Tabela: `sensor_measurements`

| Kolumna | Typ | Opis |
|---------|-----|------|
| id | Long | Klucz główny (auto-increment) |
| timestamp | Long | Znacznik czasu pomiaru |
| sensorType | String | Typ sensora ("GPS", "ACCELEROMETER") |
| latitude | Double? | Szerokość geograficzna (GPS) |
| longitude | Double? | Długość geograficzna (GPS) |
| accelerationX | Float? | Przyśpieszenie oś X (akcelerometr) |
| accelerationY | Float? | Przyśpieszenie oś Y (akcelerometr) |
| accelerationZ | Float? | Przyśpieszenie oś Z (akcelerometr) |
| photoPath | String? | Ścieżka do zdjęcia (przyszłe rozszerzenie) |
| notes | String? | Notatki (przyszłe rozszerzenie) |

