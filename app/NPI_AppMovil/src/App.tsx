import { useRef, useState } from "react";

type IconName =
  | "arrow"
  | "book"
  | "building"
  | "calendar"
  | "chevron"
  | "clock"
  | "coffee"
  | "home"
  | "location"
  | "map"
  | "menu"
  | "person"
  | "pin"
  | "search"
  | "services"
  | "wheelchair";

const icons: Record<IconName, React.ReactNode> = {
  arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
  book: <><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" /><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z" /></>,
  building: <><path d="M3 21h18M6 21V4h12v17M9 8h2M13 8h2M9 12h2M13 12h2M10 21v-5h4v5" /></>,
  calendar: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M16 3v4M8 3v4M3 10h18M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01" /></>,
  chevron: <path d="m9 18 6-6-6-6" />,
  clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
  coffee: <><path d="M4 7h13v7a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5V7Z" /><path d="M17 9h1.5a2.5 2.5 0 0 1 0 5H17M6 3h9" /></>,
  home: <><path d="m3 11 9-8 9 8" /><path d="M5 10v11h14V10M9 21v-6h6v6" /></>,
  location: <><path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" /></>,
  map: <><path d="m3 6 5-3 8 3 5-3v15l-5 3-8-3-5 3V6Z" /><path d="M8 3v15M16 6v15" /></>,
  menu: <><path d="M4 7h16M4 12h16M4 17h16" /></>,
  person: <><circle cx="12" cy="8" r="4" /><path d="M4.5 21a7.5 7.5 0 0 1 15 0" /></>,
  pin: <><path d="M12 22s7-5.4 7-12a7 7 0 1 0-14 0c0 6.6 7 12 7 12Z" /><circle cx="12" cy="10" r="2" /></>,
  search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /></>,
  services: <><path d="M4 6h16M7 6V3h10v3M6 6l1 15h10l1-15M9 10v7M15 10v7" /></>,
  wheelchair: <><circle cx="11" cy="4" r="2" /><path d="M11 7v6h5l3 5M11 9H8a5 5 0 1 0 5 7M11 13h-1" /></>,
};

function Icon({ name, size = 22 }: { name: IconName; size?: number }) {
  return (
    <svg
      aria-hidden="true"
      fill="none"
      height={size}
      viewBox="0 0 24 24"
      width={size}
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
    >
      {icons[name]}
    </svg>
  );
}

const places = [
  { name: "Aulas", detail: "Todas las plantas", icon: "building" as IconName, tone: "rose" },
  { name: "Despachos", detail: "Profesorado", icon: "person" as IconName, tone: "purple" },
  { name: "Biblioteca", detail: "Planta 1", icon: "book" as IconName, tone: "blue" },
  { name: "Cafetería", detail: "Planta baja", icon: "coffee" as IconName, tone: "orange" },
];

const week = [
  {
    short: "L",
    day: "Lunes",
    date: "14",
    classes: [
      { time: "08:30", end: "10:30", name: "Programación Web", room: "Aula A-03", tone: "red" },
      { time: "10:30", end: "12:30", name: "Diseño de Interfaces", room: "Aula B-12", tone: "purple" },
      { time: "15:30", end: "17:30", name: "Sistemas Multimedia", room: "Lab. 2", tone: "blue" },
    ],
  },
  {
    short: "M",
    day: "Martes",
    date: "15",
    classes: [
      { time: "08:30", end: "10:30", name: "Bases de Datos", room: "Aula C-05", tone: "orange" },
      { time: "12:30", end: "14:30", name: "Ingeniería del Software", room: "Aula B-08", tone: "blue" },
      { time: "17:30", end: "19:30", name: "Prácticas de Interfaces", room: "Lab. 4", tone: "purple" },
    ],
  },
  {
    short: "X",
    day: "Miércoles",
    date: "16",
    classes: [
      { time: "10:30", end: "12:30", name: "Diseño de Interfaces", room: "Aula B-12", tone: "purple" },
      { time: "15:30", end: "17:30", name: "Programación Web", room: "Lab. 1", tone: "red" },
      { time: "19:30", end: "21:30", name: "Sistemas Multimedia", room: "Aula A-10", tone: "blue" },
    ],
  },
  {
    short: "J",
    day: "Jueves",
    date: "17",
    classes: [
      { time: "08:30", end: "10:30", name: "Ingeniería del Software", room: "Aula B-08", tone: "blue" },
      { time: "12:30", end: "14:30", name: "Bases de Datos", room: "Lab. 3", tone: "orange" },
      { time: "17:30", end: "19:30", name: "Seminario de Diseño", room: "Aula C-02", tone: "green" },
    ],
  },
  {
    short: "V",
    day: "Viernes",
    date: "18",
    classes: [
      { time: "10:30", end: "12:30", name: "Prácticas de Interfaces", room: "Lab. 4", tone: "purple" },
      { time: "12:30", end: "14:30", name: "Programación Web", room: "Aula A-03", tone: "red" },
    ],
  },
];

const professors = [
  {
    id: 1,
    initials: "AM",
    name: "Ana Martínez López",
    role: "Profesora Titular",
    email: "anamartinez@ugr.es",
    faculty: "E.T.S. de Ingenierías Informática y de Telecomunicación",
    subjects: ["Diseño de Interfaces", "Sistemas Multimedia"],
    officeHours: "Martes y jueves · 12:30–14:30",
    status: "Impartiendo clase",
    location: "Aula B-12",
    floor: "Planta 1",
    tone: "red",
  },
  {
    id: 2,
    initials: "JR",
    name: "Javier Ruiz Ortega",
    role: "Profesor Contratado Doctor",
    email: "jruiz@ugr.es",
    faculty: "Facultad de Ciencias",
    subjects: ["Programación Web"],
    officeHours: "Lunes y miércoles · 17:30–19:30",
    status: "Horario de tutorías",
    location: "Despacho 2.14",
    floor: "Planta 2",
    tone: "blue",
  },
  {
    id: 3,
    initials: "EC",
    name: "Elena Castillo Pérez",
    role: "Catedrática de Universidad",
    email: "ecastillo@ugr.es",
    faculty: "E.T.S. de Ingenierías Informática y de Telecomunicación",
    subjects: ["Bases de Datos"],
    officeHours: "Miércoles · 10:30–13:30",
    status: "Sin actividad programada",
    location: "Despacho 3.08",
    floor: "Planta 3",
    tone: "purple",
  },
  {
    id: 4,
    initials: "DL",
    name: "David López Sánchez",
    role: "Profesor Ayudante Doctor",
    email: "dlopez@ugr.es",
    faculty: "E.T.S. de Ingenierías Informática y de Telecomunicación",
    subjects: ["Ingeniería del Software"],
    officeHours: "Viernes · 08:30–11:30",
    status: "Impartiendo clase",
    location: "Aula B-08",
    floor: "Planta 1",
    tone: "orange",
  },
];

const grades = {
  "1º": [
    ["Fundamentos de Programación", "8,4"],
    ["Cálculo", "7,1"],
    ["Álgebra Lineal", "6,8"],
    ["Tecnología de Computadores", "8,9"],
    ["Fundamentos Físicos", "7,6"],
  ],
  "2º": [
    ["Estructuras de Datos", "8,7"],
    ["Sistemas Operativos", "7,9"],
    ["Bases de Datos", "9,1"],
    ["Redes de Computadores", "7,4"],
    ["Ingeniería del Software", "8,2"],
  ],
  "3º": [
    ["Programación Web", "8,8"],
    ["Diseño de Interfaces", "9,3"],
    ["Sistemas Multimedia", "7,7"],
  ],
};

type MailFolder = "inbox" | "sent" | "archived";

type Mail = {
  id: number;
  folder: MailFolder;
  outgoing: boolean;
  name: string;
  email: string;
  subject: string;
  body: string;
  time: string;
  unread: boolean;
};

const mailFolders: { key: MailFolder; label: string }[] = [
  { key: "inbox", label: "Recibidos" },
  { key: "sent", label: "Enviados" },
  { key: "archived", label: "Archivados" },
];

const contacts = [
  ...professors.map((professor) => ({ name: professor.name, email: professor.email })),
  { name: "Secretaría ETSIIT", email: "secretariaetsiit@ugr.es" },
  { name: "Biblioteca UGR", email: "biblioteca@ugr.es" },
];

const initialsOf = (name: string) =>
  name.includes("@")
    ? name.slice(0, 2).toUpperCase()
    : name.split(" ").filter((word) => word.length > 2 || /^[A-ZÁÉÍÓÚ]/.test(word)).slice(0, 2).map((word) => word[0]).join("").toUpperCase();

const initialMails: Mail[] = [
  { id: 1, folder: "inbox", outgoing: false, name: "Ana Martínez", email: "anamartinez@ugr.es", subject: "Cambio de aula — Diseño de Interfaces", body: "Hola a todos:\n\nLa clase del miércoles se impartirá finalmente en el aula B-12, en la planta 1, en lugar del aula habitual.\n\nRecordad traer el prototipo avanzado para la revisión en grupo.\n\nUn saludo,\nAna", time: "10:24", unread: true },
  { id: 2, folder: "inbox", outgoing: false, name: "Secretaría ETSIIT", email: "secretariaetsiit@ugr.es", subject: "Matrícula del curso 2024/2025", body: "Estimada estudiante:\n\nTu solicitud de modificación de matrícula ha sido procesada correctamente. Puedes consultar el resguardo actualizado en la sede electrónica.\n\nAtentamente,\nSecretaría ETSIIT", time: "Ayer", unread: true },
  { id: 3, folder: "inbox", outgoing: false, name: "Biblioteca UGR", email: "biblioteca@ugr.es", subject: "Reserva disponible", body: "El ejemplar que reservaste, «Diseño de interfaces de usuario», ya está disponible para su recogida en el mostrador de préstamo de la Biblioteca de la ETSIIT.\n\nTienes 3 días para recogerlo.", time: "Ayer", unread: true },
  { id: 4, folder: "inbox", outgoing: false, name: "Javier Ruiz", email: "jruiz@ugr.es", subject: "Material de Programación Web", body: "Buenas tardes:\n\nYa tenéis publicados los ejemplos de la práctica 3 en la plataforma. Revisadlos antes de la próxima sesión de laboratorio.\n\nJavier", time: "14 oct.", unread: false },
  { id: 5, folder: "sent", outgoing: true, name: "Elena Castillo Pérez", email: "ecastillo@ugr.es", subject: "Consulta sobre la práctica 2", body: "Hola, Elena:\n\n¿Podría pasar el miércoles por tutoría para resolver unas dudas sobre la normalización de la práctica 2?\n\nMuchas gracias,\nLucía", time: "13 oct.", unread: false },
  { id: 6, folder: "archived", outgoing: false, name: "Secretaría ETSIIT", email: "secretariaetsiit@ugr.es", subject: "Bienvenida al curso 2024/2025", body: "Te damos la bienvenida al nuevo curso académico. Consulta el calendario académico y los horarios en la web del centro.", time: "9 sept.", unread: false },
];

const MONTHS = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"];
const MONTHS_SHORT = ["ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"];

const campuses = [
  { key: "etsiit", name: "ETSIIT", campus: "CAMPUS AYNADAMAR", full: "Escuela Técnica Superior de Ingenierías Informática y de Telecomunicación", distance: "1,2 km", hours: "Abierto hasta 21:30" },
  { key: "sciences", name: "Ciencias", campus: "CAMPUS FUENTENUEVA", full: "Facultad de Ciencias", distance: "2,8 km", hours: "Abierto hasta 21:00" },
  { key: "cartuja", name: "Cartuja", campus: "CAMPUS CARTUJA", full: "Facultades de Psicología, Ciencias Económicas y Empresariales, Farmacia y Odontología", distance: "0,9 km", hours: "Abierto hasta 21:30" },
];

const floorRooms: Record<string, string[]> = {
  P2: ["Despachos\n2.01–2.20", "Sala de juntas", "Laboratorio 5", "Laboratorio 6", "Seminario 2"],
  P1: ["Aulas\nB-10–B-13", "Biblioteca", "Sala de estudio", "Laboratorio 2", "Aula B-08"],
  PB: ["Aulas\n0.1–0.4", "Secretaría", "Conserjería", "Cafetería", "Biblioteca"],
  S1: ["Laboratorios\nS1.1–S1.4", "Taquillas", "Almacén", "Reprografía", "Aparcamiento de bicis"],
};

const pointsOfInterest: { name: string; icon: IconName; detail: string; route: string }[] = [
  { name: "Biblioteca", icon: "book", detail: "Planta 1", route: "Biblioteca" },
  { name: "Cafetería", icon: "coffee", detail: "Planta baja", route: "Cafetería" },
  { name: "Secretaría", icon: "services", detail: "Planta baja", route: "Secretaría" },
  { name: "Conserjería", icon: "person", detail: "Entrada", route: "Conserjería" },
  { name: "Baños cercanos", icon: "location", detail: "A 24 m", route: "Baños más cercanos" },
  { name: "Sala de estudio", icon: "book", detail: "Planta 1", route: "Sala de estudio" },
  { name: "Reprografía", icon: "services", detail: "Sótano 1", route: "Reprografía" },
];

const getRouteSteps = (accessible: boolean, destination: string) => [
  { icon: "↑", text: "Continúa recto por el pasillo", distance: "32 m" },
  { icon: "←", text: "Gira a la izquierda en el pasillo principal", distance: "18 m" },
  accessible
    ? { icon: "⇅", text: "Toma el ascensor hasta la planta 1", distance: "1 planta" }
    : { icon: "⇅", text: "Sube por las escaleras a la planta 1", distance: "1 planta" },
  { icon: "→", text: "Gira a la derecha al salir", distance: accessible ? "64 m" : "24 m" },
  { icon: "✓", text: `Has llegado: ${destination}`, distance: "" },
];

type HelpTopic = "navigation" | "schedule" | "account";

const helpTopics: { key: HelpTopic; icon: IconName; title: string; detail: string }[] = [
  { key: "navigation", icon: "map", title: "Problemas con la navegación", detail: "GPS, rutas o planos del edificio" },
  { key: "schedule", icon: "calendar", title: "Horario y calendario", detail: "Clases, entregas y sincronización" },
  { key: "account", icon: "person", title: "Cuenta y acceso", detail: "Inicio de sesión y datos personales" },
];

const faqs: { topic: HelpTopic; question: string; answer: string }[] = [
  { topic: "navigation", question: "¿Cómo llego a mi próxima clase?", answer: "En Inicio, pulsa «Guiarme al aula» en la tarjeta de tu próxima clase. También puedes pulsar «Cómo llegar» en cualquier clase de tu horario." },
  { topic: "navigation", question: "¿Puedo evitar las escaleras?", answer: "Sí. En la pantalla de ruta activa «Ruta accesible» y te guiaremos usando ascensores y rampas." },
  { topic: "navigation", question: "La ubicación no es precisa dentro del edificio", answer: "Dentro de los edificios el GPS pierde precisión. Activa el wifi y el Bluetooth para mejorar la localización, o selecciona la planta manualmente en el plano." },
  { topic: "navigation", question: "¿Cómo cambio de planta en el plano?", answer: "En Mapa › Ver plano del edificio, usa el selector de plantas (P2, P1, PB, S1) situado en la parte superior." },
  { topic: "schedule", question: "¿De dónde sale mi horario?", answer: "Tu horario se genera automáticamente a partir de las asignaturas en las que estás matriculada en la UGR." },
  { topic: "schedule", question: "¿Cómo añado una entrega o un examen?", answer: "Ve a Más › Calendario, selecciona el día y pulsa «+ Añadir». Puedes indicar la asignatura, la hora y una descripción." },
  { topic: "schedule", question: "Una clase ha cambiado de aula y no se refleja", answer: "Los cambios de aula los comunica el profesorado y pueden tardar unas horas en actualizarse. Revisa también tus mensajes." },
  { topic: "account", question: "He olvidado mi contraseña", answer: "La contraseña es la de tu cuenta institucional UGR. Puedes restablecerla desde el Acceso Identificado de la UGR con tu DNI y tu correo personal." },
  { topic: "account", question: "¿Cómo cierro sesión?", answer: "Pulsa tu avatar en la esquina superior derecha y elige «Cerrar sesión», o hazlo desde Mi perfil." },
  { topic: "account", question: "Mis datos personales no son correctos", answer: "Los datos académicos proceden de la Secretaría del centro. Para corregirlos, contacta con Secretaría o crea una incidencia." },
];

type Ticket = { id: string; topic: HelpTopic; subject: string; description: string; place: string; status: "Abierta" | "En revisión" | "Resuelta"; date: string };

const initialTickets: Ticket[] = [
  { id: "INC-0142", topic: "navigation", subject: "El plano de la planta S1 no carga", description: "Al seleccionar la planta S1 en el plano de la ETSIIT no aparece ningún espacio.", place: "ETSIIT · Planta S1", status: "Resuelta", date: "2024-10-08" },
];

type CalendarEvent = {
  id: number;
  title: string;
  subject: string;
  date: string;
  time: string;
  type: "Entrega" | "Examen";
  description: string;
};

const subjects = Array.from(new Set(week.flatMap((day) => day.classes.map((lesson) => lesson.name))));

const formatDate = (date: string) => {
  const text = new Date(`${date}T00:00`).toLocaleDateString("es-ES", { weekday: "long", day: "numeric", month: "long" });
  return text.charAt(0).toUpperCase() + text.slice(1);
};

const TODAY = "2024-10-16";

const daysUntil = (date: string) => Math.round((new Date(`${date}T00:00`).getTime() - new Date(`${TODAY}T00:00`).getTime()) / 86400000);

const formatCountdown = (date: string) => {
  const days = daysUntil(date);
  if (days === 0) return "Hoy";
  if (days === 1) return "Mañana";
  if (days > 1) return `Faltan ${days} días`;
  return days === -1 ? "Fue ayer" : `Hace ${-days} días`;
};

const formatEventTime = (event: CalendarEvent) => `${event.type === "Examen" ? "Empieza a las" : "Entrega hasta las"} ${event.time}`;

export default function App() {
  const [authenticated, setAuthenticated] = useState(false);
  const [accessible, setAccessible] = useState(false);
  const [destination, setDestination] = useState("Aula B-12");
  const [query, setQuery] = useState("");
  const [showRoute, setShowRoute] = useState(false);
  const [activeView, setActiveView] = useState<"home" | "schedule" | "professors" | "map" | "profile" | "more" | "grades" | "calendar" | "event" | "messages" | "help" | "accessibility">("home");
  const [selectedDay, setSelectedDay] = useState(0);
  const [professorQuery, setProfessorQuery] = useState("");
  const [selectedProfessor, setSelectedProfessor] = useState<number | null>(null);
  const [mapMode, setMapMode] = useState<"city" | "indoor">("city");
  const [selectedCampus, setSelectedCampus] = useState<string | null>(null);
  const [mapSearchOpen, setMapSearchOpen] = useState(false);
  const [mapQuery, setMapQuery] = useState("");
  const [indoorSearchOpen, setIndoorSearchOpen] = useState(false);
  const [indoorQuery, setIndoorQuery] = useState("");
  const [planZoom, setPlanZoom] = useState(1);
  const [routeMenu, setRouteMenu] = useState(false);
  const [navStep, setNavStep] = useState<number | null>(null);
  const [professorMenu, setProfessorMenu] = useState(false);
  const [recovery, setRecovery] = useState<"closed" | "open" | "sent">("closed");
  const [loginEmail, setLoginEmail] = useState("lucia@correo.ugr.es");
  const [profile, setProfile] = useState({ preferredName: "Lucía", phone: "+34 600 000 000", personalEmail: "lucia.martin@ejemplo.com" });
  const [profileDraft, setProfileDraft] = useState(profile);
  const [editingProfile, setEditingProfile] = useState(false);
  const [largeText, setLargeText] = useState(false);
  const [highContrast, setHighContrast] = useState(false);
  const [reduceMotion, setReduceMotion] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const toastTimer = useRef<number | undefined>(undefined);
  const [campusFloor, setCampusFloor] = useState("PB");
  const [poiExpanded, setPoiExpanded] = useState(true);
  const [accountMenu, setAccountMenu] = useState(false);
  const [gradeCourse, setGradeCourse] = useState<keyof typeof grades>("1º");
  const [showEventForm, setShowEventForm] = useState(false);
  const [selectedDate, setSelectedDate] = useState(TODAY);
  const [calendarMonth, setCalendarMonth] = useState(TODAY.slice(0, 7));
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const [eventBackView, setEventBackView] = useState<"more" | "calendar">("calendar");
  const [editingEventId, setEditingEventId] = useState<number | null>(null);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [mails, setMails] = useState<Mail[]>(initialMails);
  const [mailFolder, setMailFolder] = useState<MailFolder>("inbox");
  const [mailQuery, setMailQuery] = useState("");
  const [mailMode, setMailMode] = useState<"list" | "read" | "compose">("list");
  const [openMailId, setOpenMailId] = useState<number | null>(null);
  const [composeTo, setComposeTo] = useState("");
  const [composeSubject, setComposeSubject] = useState("");
  const [composeBody, setComposeBody] = useState("");
  const [mailNotice, setMailNotice] = useState<string | null>(null);
  const [confirmMailDelete, setConfirmMailDelete] = useState(false);
  const [helpMode, setHelpMode] = useState<"home" | "topic" | "report">("home");
  const [helpTopic, setHelpTopic] = useState<HelpTopic>("navigation");
  const [helpQuery, setHelpQuery] = useState("");
  const [openFaq, setOpenFaq] = useState<string | null>(null);
  const [tickets, setTickets] = useState<Ticket[]>(initialTickets);
  const [ticketTopic, setTicketTopic] = useState<HelpTopic>("navigation");
  const [ticketSubject, setTicketSubject] = useState("");
  const [ticketDescription, setTicketDescription] = useState("");
  const [ticketPlace, setTicketPlace] = useState("");
  const [helpNotice, setHelpNotice] = useState<string | null>(null);
  const [eventTitle, setEventTitle] = useState("");
  const [eventSubject, setEventSubject] = useState(subjects[0]);
  const [eventTime, setEventTime] = useState("23:59");
  const [eventDescription, setEventDescription] = useState("");
  const [eventType, setEventType] = useState<CalendarEvent["type"]>("Entrega");
  const [calendarEvents, setCalendarEvents] = useState<CalendarEvent[]>([
    { id: 1, title: "Prototipo de Interfaces", subject: "Diseño de Interfaces", date: "2024-10-18", time: "23:59", type: "Entrega", description: "Prototipo navegable en Figma y memoria de diseño." },
    { id: 2, title: "Examen de Bases de Datos", subject: "Bases de Datos", date: "2024-10-24", time: "10:30", type: "Examen", description: "Temas 1 a 4. Aula C-05." },
    { id: 3, title: "Práctica de Programación Web", subject: "Programación Web", date: "2024-10-29", time: "23:59", type: "Entrega", description: "Práctica 3 en la plataforma." },
  ]);

  const sortedEvents = [...calendarEvents].sort((a, b) => `${a.date} ${a.time}`.localeCompare(`${b.date} ${b.time}`));
  const dayEvents = sortedEvents.filter((event) => event.date === selectedDate);
  const nextDelivery = sortedEvents.find((event) => event.type === "Entrega" && event.date >= TODAY);
  const selectedEvent = calendarEvents.find((event) => event.id === selectedEventId);

  const openEvent = (event: CalendarEvent, from: "more" | "calendar") => {
    setSelectedEventId(event.id);
    setEventBackView(from);
    setEditingEventId(null);
    setConfirmDelete(false);
    setActiveView("event");
  };

  const startNewEvent = () => {
    setEditingEventId(null);
    setEventTitle("");
    setEventDescription("");
    setShowEventForm(true);
  };

  const startEditEvent = (event: CalendarEvent) => {
    setEventType(event.type);
    setEventSubject(event.subject);
    setEventTitle(event.title);
    selectDate(event.date);
    setEventTime(event.time);
    setEventDescription(event.description);
    setConfirmDelete(false);
    setEditingEventId(event.id);
  };

  const closeEventForm = () => {
    if (editingEventId) setEditingEventId(null);
    else setShowEventForm(false);
  };

  const saveEvent = (submit: React.FormEvent) => {
    submit.preventDefault();
    const data = {
      title: eventTitle.trim() || `${eventType} de ${eventSubject}`,
      subject: eventSubject,
      date: selectedDate,
      time: eventTime,
      type: eventType,
      description: eventDescription.trim(),
    };
    if (editingEventId) {
      setCalendarEvents(calendarEvents.map((event) => (event.id === editingEventId ? { ...event, ...data } : event)));
      setEditingEventId(null);
    } else {
      setCalendarEvents([...calendarEvents, { id: Date.now(), ...data }]);
      setShowEventForm(false);
    }
    setEventTitle("");
    setEventDescription("");
  };

  const deleteEvent = (id: number) => {
    setCalendarEvents(calendarEvents.filter((event) => event.id !== id));
    setSelectedEventId(null);
    setConfirmDelete(false);
    setActiveView(eventBackView);
  };

  const unreadCount = mails.filter((mail) => mail.folder === "inbox" && mail.unread).length;
  const openMail = mails.find((mail) => mail.id === openMailId);
  const folderMails = mails
    .filter((mail) => mail.folder === mailFolder)
    .filter((mail) => `${mail.name} ${mail.email} ${mail.subject} ${mail.body}`.toLowerCase().includes(mailQuery.toLowerCase()));

  const goToMessages = () => {
    setMailMode("list");
    setMailNotice(null);
    setActiveView("messages");
  };

  const readMail = (mail: Mail) => {
    setMails(mails.map((item) => (item.id === mail.id ? { ...item, unread: false } : item)));
    setOpenMailId(mail.id);
    setConfirmMailDelete(false);
    setMailNotice(null);
    setMailMode("read");
  };

  const startCompose = (to = "", subject = "") => {
    setComposeTo(to);
    setComposeSubject(subject);
    setComposeBody("");
    setMailNotice(null);
    setMailMode("compose");
  };

  const sendMail = (submit: React.FormEvent) => {
    submit.preventDefault();
    const to = composeTo.trim();
    const contact = contacts.find((item) => item.email === to || item.name.toLowerCase() === to.toLowerCase());
    const name = contact?.name ?? to;
    setMails([{
      id: Date.now(),
      folder: "sent",
      outgoing: true,
      name,
      email: contact?.email ?? to,
      subject: composeSubject.trim() || "(Sin asunto)",
      body: composeBody.trim(),
      time: "Ahora",
      unread: false,
    }, ...mails]);
    setMailFolder("sent");
    setMailQuery("");
    setMailMode("list");
    setMailNotice(`Mensaje enviado a ${name}`);
  };

  const moveMail = (mail: Mail, folder: MailFolder) => {
    setMails(mails.map((item) => (item.id === mail.id ? { ...item, folder } : item)));
    setMailMode("list");
    setMailNotice(folder === "archived" ? "Mensaje archivado" : `Mensaje movido a ${folder === "sent" ? "Enviados" : "Recibidos"}`);
  };

  const deleteMail = (mail: Mail) => {
    setMails(mails.filter((item) => item.id !== mail.id));
    setConfirmMailDelete(false);
    setMailMode("list");
    setMailNotice("Mensaje eliminado");
  };

  const goToHelp = () => {
    setHelpMode("home");
    setHelpQuery("");
    setOpenFaq(null);
    setHelpNotice(null);
    setActiveView("help");
  };

  const startTicket = (topic: HelpTopic = "navigation") => {
    setTicketTopic(topic);
    setTicketSubject("");
    setTicketDescription("");
    setTicketPlace("");
    setHelpNotice(null);
    setHelpMode("report");
  };

  const sendTicket = (submit: React.FormEvent) => {
    submit.preventDefault();
    const id = `INC-${String(143 + tickets.length - initialTickets.length).padStart(4, "0")}`;
    setTickets([{ id, topic: ticketTopic, subject: ticketSubject.trim(), description: ticketDescription.trim(), place: ticketPlace.trim(), status: "Abierta", date: TODAY }, ...tickets]);
    setHelpMode("home");
    setHelpNotice(`Incidencia ${id} enviada. Te responderemos en un plazo de 48 h.`);
  };

  const renderFaq = (faq: (typeof faqs)[number]) => (
    <div className={`faq-item ${openFaq === faq.question ? "open" : ""}`} key={faq.question}>
      <button aria-expanded={openFaq === faq.question} onClick={() => setOpenFaq(openFaq === faq.question ? null : faq.question)} type="button">
        <strong>{faq.question}</strong>
        <i>⌄</i>
      </button>
      {openFaq === faq.question && <p>{faq.answer}</p>}
    </div>
  );

  const eventForm = (
    <form className="event-form" onSubmit={saveEvent}>
      <div><strong>{editingEventId ? "Editar evento" : `Añadir a ${formatDate(selectedDate).toLowerCase()}`}</strong><button onClick={closeEventForm} type="button" aria-label="Cerrar">×</button></div>
      <div className="event-type-toggle" role="radiogroup" aria-label="Tipo de evento">
        {(["Entrega", "Examen"] as const).map((type) => (
          <button
            aria-checked={eventType === type}
            className={eventType === type ? "active" : ""}
            key={type}
            onClick={() => {
              setEventType(type);
              setEventTime(type === "Examen" ? "09:00" : "23:59");
            }}
            role="radio"
            type="button"
          >
            {type}
          </button>
        ))}
      </div>
      <label>
        <span>Asignatura</span>
        <select onChange={(event) => setEventSubject(event.target.value)} value={eventSubject}>
          {subjects.map((subject) => <option key={subject}>{subject}</option>)}
        </select>
      </label>
      <label>
        <span>Título <small>(opcional)</small></span>
        <input onChange={(event) => setEventTitle(event.target.value)} placeholder={`${eventType} de ${eventSubject}`} value={eventTitle} />
      </label>
      <div className="event-form-row">
        <label>
          <span>Fecha</span>
          <input onChange={(event) => event.target.value && selectDate(event.target.value)} required type="date" value={selectedDate} />
        </label>
        <label>
          <span>{eventType === "Examen" ? "Hora del examen" : "Hora de entrega"}</span>
          <input onChange={(event) => setEventTime(event.target.value)} required type="time" value={eventTime} />
        </label>
      </div>
      <label>
        <span>Descripción</span>
        <textarea onChange={(event) => setEventDescription(event.target.value)} placeholder={eventType === "Examen" ? "Temas, aula, material permitido..." : "Qué hay que entregar y dónde..."} rows={3} value={eventDescription} />
      </label>
      <button type="submit">{editingEventId ? "Guardar cambios" : `Guardar ${eventType.toLowerCase()}`}</button>
    </form>
  );

  const renderEvent = (event: CalendarEvent) => (
    <button className="event-item" key={event.id} onClick={() => openEvent(event, "calendar")} type="button">
      <span className={event.type === "Examen" ? "exam" : ""}>{Number(event.date.slice(-2))}<small>{MONTHS_SHORT[Number(event.date.slice(5, 7)) - 1]}</small></span>
      <div>
        <small>{event.type.toUpperCase()} · {event.subject}</small>
        <strong>{event.title}</strong>
        <p><Icon name="clock" size={11} /> {formatEventTime(event)}</p>
        {event.description && <p className="event-description">{event.description}</p>}
      </div>
      <Icon name="chevron" size={16} />
    </button>
  );

  const openRoute = (place = destination) => {
    setDestination(place);
    setRouteMenu(false);
    setNavStep(null);
    setShowRoute(true);
  };

  const showToast = (message: string) => {
    setToast(message);
    window.clearTimeout(toastTimer.current);
    toastTimer.current = window.setTimeout(() => setToast(null), 2800);
  };

  const copyText = (text: string, message: string) => {
    navigator.clipboard?.writeText(text).catch(() => undefined);
    showToast(message);
  };

  const selectDate = (date: string) => {
    setSelectedDate(date);
    setCalendarMonth(date.slice(0, 7));
  };

  const [calendarYear, calendarMonthNumber] = calendarMonth.split("-").map(Number);
  const monthLabel = `${MONTHS[calendarMonthNumber - 1].charAt(0).toUpperCase()}${MONTHS[calendarMonthNumber - 1].slice(1)} ${calendarYear}`;
  const monthCells = [
    ...Array<null>((new Date(calendarYear, calendarMonthNumber - 1, 1).getDay() + 6) % 7).fill(null),
    ...Array.from({ length: new Date(calendarYear, calendarMonthNumber, 0).getDate() }, (_, index) => index + 1),
  ];
  const shiftMonth = (delta: number) => {
    const date = new Date(calendarYear, calendarMonthNumber - 1 + delta, 1);
    setCalendarMonth(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`);
  };

  const currentCampus = campuses.find((campus) => campus.key === (selectedCampus ?? "etsiit"))!;
  const routeSteps = getRouteSteps(accessible, destination);

  const finishNavigation = () => {
    setNavStep(null);
    setShowRoute(false);
    showToast(`Has llegado a ${destination}`);
  };

  const shareRoute = () => {
    setRouteMenu(false);
    const text = `Ruta hasta ${destination} · ${currentCampus.name}, UGR Campus`;
    if (navigator.share) navigator.share({ title: "Ruta UGR Campus", text }).catch(() => undefined);
    else copyText(text, "Ruta copiada al portapapeles");
  };

  const downloadTranscript = () => {
    const rows = Object.entries(grades).flatMap(([course, list]) => list.map(([subject, grade]) => `${course};${subject};6;${grade}`));
    const csv = ["Expediente académico;Lucía Martín García", "Titulación;Grado en Ingeniería Informática", "", "Curso;Asignatura;Créditos ECTS;Calificación", ...rows, "", "Nota media;7,8"].join("\n");
    const url = URL.createObjectURL(new Blob([`\ufeff${csv}`], { type: "text/csv;charset=utf-8" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = "expediente-ugr.csv";
    link.click();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    showToast("Expediente descargado");
  };

  if (!authenticated) {
    return (
      <div className="page">
        <div className="phone-app login-screen">
          <div className="login-hero">
            <div className="login-brand">UGR</div>
            <span>UGR CAMPUS</span>
            <h1>Tu facultad,<br />más cerca.</h1>
            <p>Encuentra tus clases y muévete por el centro sin perderte.</p>
          </div>
          <form
            className="login-card"
            onSubmit={(event) => {
              event.preventDefault();
              setAuthenticated(true);
            }}
          >
            <div className="login-heading">
              <h2>Inicia sesión</h2>
              <p>Utiliza tu cuenta institucional de la UGR.</p>
            </div>
            <label>
              Correo institucional
              <span><Icon name="person" size={19} /><input onChange={(event) => setLoginEmail(event.target.value)} required type="email" value={loginEmail} /></span>
            </label>
            <label>
              Contraseña
              <span><Icon name="services" size={19} /><input defaultValue="universidad" required type="password" /></span>
            </label>
            <div className="login-options">
              <label><input type="checkbox" defaultChecked /> Recordarme</label>
              <button onClick={() => setRecovery(recovery === "closed" ? "open" : "closed")} type="button">¿Has olvidado la contraseña?</button>
            </div>
            {recovery === "open" && (
              <div className="recovery-box">
                <strong>Recuperar contraseña</strong>
                <p>Te enviaremos un enlace para restablecerla a <b>{loginEmail || "tu correo institucional"}</b>.</p>
                <div>
                  <button onClick={() => setRecovery("closed")} type="button">Cancelar</button>
                  <button className="primary" disabled={!loginEmail.includes("@")} onClick={() => setRecovery("sent")} type="button">Enviar enlace</button>
                </div>
              </div>
            )}
            {recovery === "sent" && (
              <div className="recovery-box sent">
                <strong>✓ Enlace enviado</strong>
                <p>Revisa la bandeja de {loginEmail}. El enlace caduca en 30 minutos.</p>
              </div>
            )}
            <button className="login-button" type="submit">Entrar <Icon name="arrow" size={20} /></button>
            <p className="login-help">Acceso seguro mediante identidad UGR</p>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className={`phone-app ${largeText ? "large-text" : ""} ${highContrast ? "high-contrast" : ""} ${reduceMotion ? "reduce-motion" : ""}`}>
        <header className="mobile-header">
          <div className="header-brand">
            <div className="brand-mark">UGR</div>
            <div>
              <span>Hola, {profile.preferredName}</span>
              <strong>Facultad de Ciencias</strong>
            </div>
          </div>
          <button className="avatar" onClick={() => setAccountMenu(!accountMenu)} type="button" aria-label="Abrir menú de usuario">LM</button>
          {accountMenu && (
            <div className="account-menu">
              <div><span>LM</span><p><strong>Lucía Martín</strong><small>Estudiante</small></p></div>
              <button onClick={() => {
                setAccountMenu(false);
                setActiveView("profile");
              }} type="button"><Icon name="person" size={18} /> Mi perfil <Icon name="chevron" size={15} /></button>
              <button className="logout" onClick={() => {
                setAccountMenu(false);
                setActiveView("home");
                setAuthenticated(false);
              }} type="button"><Icon name="arrow" size={18} /> Cerrar sesión</button>
            </div>
          )}
        </header>

        {activeView === "home" ? (
        <main className="mobile-content">
          <section className="intro">
            <h1>¿A dónde vamos?</h1>
            <p>Te guiamos por el interior del centro.</p>
          </section>

          <div className="search-box">
            <Icon name="search" size={21} />
            <input
              aria-label="Buscar destino"
              onChange={(event) => setQuery(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter" && query.trim()) openRoute(query.trim());
              }}
              placeholder="Aula, profesor o servicio"
              type="search"
              value={query}
            />
            <button
              aria-label="Buscar"
              disabled={!query.trim()}
              onClick={() => openRoute(query.trim())}
              type="button"
            >
              <Icon name="arrow" size={18} />
            </button>
          </div>

          <section className="next-class">
            <div className="class-topline">
              <span><i /> TU PRÓXIMA CLASE</span>
              <span>en 18 min</span>
            </div>
            <div className="class-main">
              <div className="calendar-tile">
                <span>10:30</span>
                <strong>HOY</strong>
              </div>
              <div className="class-info">
                <h2>Diseño de Interfaces</h2>
                <p><Icon name="pin" size={16} /> Aula B-12 · Planta 1</p>
              </div>
            </div>
            <button className="route-button" onClick={() => openRoute("Aula B-12")} type="button">
              <Icon name="location" size={19} />
              Guiarme al aula
              <Icon name="arrow" size={19} />
            </button>
          </section>

          <section className="mobile-section">
            <div className="section-title">
              <h2>Accesos rápidos</h2>
            </div>
            <div className="quick-grid">
              <button type="button" onClick={() => {
                setSelectedProfessor(null);
                setActiveView("professors");
              }}>
                <span className="quick-icon professor"><Icon name="person" /></span>
                <strong>Buscar<br />profesor</strong>
                <Icon name="chevron" size={16} />
              </button>
              <button type="button" onClick={() => setActiveView("schedule")}>
                <span className="quick-icon schedule"><Icon name="calendar" /></span>
                <strong>Mi<br />horario</strong>
                <Icon name="chevron" size={16} />
              </button>
            </div>
          </section>

          <section className="mobile-section">
            <div className="section-title">
              <h2>Explora el centro</h2>
              <button type="button" onClick={() => {
                setSelectedCampus("etsiit");
                setMapMode("indoor");
                setActiveView("map");
              }}>Ver mapa</button>
            </div>
            <div className="place-row">
              {places.map((place) => (
                <button key={place.name} onClick={() => {
                  if (place.name === "Aulas") {
                    setSelectedCampus("etsiit");
                    setMapMode("indoor");
                    setActiveView("map");
                  } else if (place.name === "Despachos") {
                    setSelectedProfessor(null);
                    setActiveView("professors");
                  } else {
                    openRoute(place.name);
                  }
                }} type="button">
                  <span className={`place-icon ${place.tone}`}><Icon name={place.icon} /></span>
                  <strong>{place.name}</strong>
                  <small>{place.detail}</small>
                </button>
              ))}
            </div>
          </section>

        </main>
        ) : activeView === "schedule" ? (
          <main className="schedule-content">
            <section className="schedule-heading">
              <span>SEMANA 14–18 OCT.</span>
              <h1>Mi horario</h1>
              <p>Grado en Ingeniería Informática · 3º</p>
            </section>

            <div className="day-selector">
              {week.map((item, index) => (
                <button
                  className={selectedDay === index ? "active" : ""}
                  key={item.day}
                  onClick={() => setSelectedDay(index)}
                  type="button"
                >
                  <span>{item.short}</span>
                  <strong>{item.date}</strong>
                  <i />
                </button>
              ))}
            </div>

            <section className="day-schedule">
              <div className="day-summary">
                <div><h2>{week[selectedDay].day}, {week[selectedDay].date}</h2><span>{week[selectedDay].classes.length} clases</span></div>
                <button onClick={() => {
                  selectDate(`2024-10-${week[selectedDay].date}`);
                  setShowEventForm(false);
                  setActiveView("calendar");
                }} type="button" aria-label="Abrir calendario"><Icon name="calendar" size={19} /></button>
              </div>

              <div className="timeline">
                {week[selectedDay].classes.map((lesson, index) => (
                  <div className="timeline-block" key={lesson.name}>
                    {lesson.time >= "15:30" && (index === 0 || week[selectedDay].classes[index - 1].time < "15:30") && (
                      <div className="day-break">
                        <span>14:30</span>
                        <div><Icon name="coffee" size={17} /> Descanso de mediodía</div>
                        <span>15:30</span>
                      </div>
                    )}
                    <div className="timeline-row">
                      <div className="time-column">
                        <strong>{lesson.time}</strong>
                        <span>{lesson.end}</span>
                      </div>
                      <div className="time-line"><i className={index === 0 ? "current" : ""} /></div>
                      <article className={`lesson-card ${lesson.tone}`}>
                        <span>{index === 0 && selectedDay === 0 ? "SIGUIENTE" : "CLASE"}</span>
                        <h3>{lesson.name}</h3>
                        <p><Icon name="pin" size={15} /> {lesson.room}</p>
                        <button onClick={() => openRoute(lesson.room)} type="button">
                          Cómo llegar <Icon name="arrow" size={16} />
                        </button>
                      </article>
                    </div>
                  </div>
                ))}
                <div className="day-range"><span>Jornada académica</span><strong>08:30 — 21:30</strong></div>
              </div>
            </section>
          </main>
        ) : activeView === "professors" ? (
          <main className="professors-content">
            {selectedProfessor === null ? (
              <>
                <section className="professors-heading">
                  <button onClick={() => setActiveView("home")} type="button" aria-label="Volver">‹</button>
                  <div>
                    <span>DIRECTORIO UGR</span>
                    <h1>Buscar profesor</h1>
                    <p>Encuentra al profesorado de tus asignaturas.</p>
                  </div>
                </section>

                <div className="professor-search">
                  <Icon name="search" size={20} />
                  <input
                    aria-label="Buscar profesor por nombre"
                    onChange={(event) => setProfessorQuery(event.target.value)}
                    placeholder="Buscar por nombre o asignatura"
                    type="search"
                    value={professorQuery}
                  />
                </div>

                <section className="my-professors">
                  <div className="professor-section-title">
                    <div><h2>{professorQuery ? "Resultados" : "Mis profesores"}</h2><span>{professorQuery ? "Directorio UGR" : "Según tus asignaturas"}</span></div>
                    <span>{professors.filter((professor) => `${professor.name} ${professor.subjects.join(" ")}`.toLowerCase().includes(professorQuery.toLowerCase())).length}</span>
                  </div>
                  <div className="professor-list">
                    {professors
                      .filter((professor) => `${professor.name} ${professor.subjects.join(" ")}`.toLowerCase().includes(professorQuery.toLowerCase()))
                      .map((professor) => (
                        <button key={professor.id} onClick={() => {
                          setProfessorMenu(false);
                          setSelectedProfessor(professor.id);
                        }} type="button">
                          <span className={`professor-avatar ${professor.tone}`}>{professor.initials}</span>
                          <span className="professor-main">
                            <strong>{professor.name}</strong>
                            <small>{professor.subjects[0]}</small>
                            <i className={professor.status === "Sin actividad programada" ? "away" : ""}>{professor.status}</i>
                          </span>
                          <Icon name="chevron" size={18} />
                        </button>
                      ))}
                  </div>
                </section>

                <div className="directory-note">
                  <Icon name="search" size={19} />
                  <div><strong>¿No aparece en tu lista?</strong><span>Busca por nombre para consultar todo el directorio de profesorado UGR.</span></div>
                </div>
              </>
            ) : (() => {
              const professor = professors.find((item) => item.id === selectedProfessor)!;
              return (
                <>
                  <section className="profile-header">
                    <button onClick={() => setSelectedProfessor(null)} type="button" aria-label="Volver">‹</button>
                    <span>Perfil del profesor</span>
                    <button aria-expanded={professorMenu} onClick={() => setProfessorMenu(!professorMenu)} type="button" aria-label="Más opciones">•••</button>
                    {professorMenu && (
                      <div className="pop-menu">
                        <button onClick={() => {
                          setProfessorMenu(false);
                          startCompose(professor.email);
                          setActiveView("messages");
                        }} type="button"><Icon name="services" size={17} /> Enviar mensaje</button>
                        <button onClick={() => {
                          setProfessorMenu(false);
                          copyText(professor.email, "Correo copiado al portapapeles");
                        }} type="button"><Icon name="person" size={17} /> Copiar correo</button>
                        <button onClick={() => {
                          setProfessorMenu(false);
                          openRoute(professor.location);
                        }} type="button"><Icon name="location" size={17} /> Cómo llegar</button>
                      </div>
                    )}
                  </section>

                  <section className="professor-profile">
                    <span className={`profile-avatar ${professor.tone}`}>{professor.initials}</span>
                    <h1>{professor.name}</h1>
                    <p>{professor.role}</p>
                    <span className="profile-subject">{professor.subjects.join(" · ")}</span>
                  </section>

                  <section className="professor-details">
                    <div>
                      <span><Icon name="person" size={18} /></span>
                      <div><small>CORREO ELECTRÓNICO</small><strong>{professor.email}</strong></div>
                    </div>
                    <div>
                      <span><Icon name="building" size={18} /></span>
                      <div><small>CENTRO</small><strong>{professor.faculty}</strong></div>
                    </div>
                    <div>
                      <span><Icon name="calendar" size={18} /></span>
                      <div><small>HORARIO DE TUTORÍAS</small><strong>{professor.officeHours}</strong></div>
                    </div>
                  </section>

                  <section className="professor-location">
                    <div className="location-status">
                      <span><i className={professor.status === "Sin actividad programada" ? "away" : ""} /> LOCALIZACIÓN ESTIMADA AHORA</span>
                      <small>Según su horario académico</small>
                    </div>
                    <div className="location-main">
                      <span><Icon name={professor.status === "Horario de tutorías" ? "person" : "building"} /></span>
                      <div>
                        <small>{professor.status}</small>
                        <h2>{professor.location}</h2>
                        <p>{professor.floor} · Edificio principal</p>
                      </div>
                    </div>
                    <button onClick={() => openRoute(professor.location)} type="button">
                      <Icon name="location" size={19} />
                      Guiarme hasta allí
                      <Icon name="arrow" size={19} />
                    </button>
                    <p className="estimate-note">La ubicación es una estimación basada en clases y tutorías programadas.</p>
                  </section>
                </>
              );
            })()}
          </main>
        ) : activeView === "map" ? (
          <main className="campus-map-content">
            {mapMode === "city" ? (
              <section className="granada-map">
                <div className="city-map-toolbar">
                  <div>
                    <span>Mapa universitario</span>
                    <strong>Granada</strong>
                  </div>
                  <button aria-expanded={mapSearchOpen} onClick={() => {
                    setMapSearchOpen(!mapSearchOpen);
                    setMapQuery("");
                  }} type="button" aria-label="Buscar en el mapa"><Icon name="search" size={19} /></button>
                </div>
                {mapSearchOpen && (
                  <div className="map-search">
                    <div className="inbox-search">
                      <Icon name="search" size={18} />
                      <input aria-label="Buscar centro" autoFocus onChange={(event) => setMapQuery(event.target.value)} placeholder="Buscar centro o campus" type="search" value={mapQuery} />
                    </div>
                    {campuses
                      .filter((campus) => `${campus.name} ${campus.campus} ${campus.full}`.toLowerCase().includes(mapQuery.toLowerCase()))
                      .map((campus) => (
                        <button key={campus.key} onClick={() => {
                          setSelectedCampus(campus.key);
                          setMapSearchOpen(false);
                        }} type="button">
                          <Icon name="building" size={17} />
                          <span><strong>{campus.name}</strong><small>{campus.campus} · {campus.distance}</small></span>
                        </button>
                      ))}
                  </div>
                )}

                <div className="city-map-canvas">
                  <svg className="city-roads" viewBox="0 0 430 700" preserveAspectRatio="none" aria-hidden="true">
                    <path className="river" d="M52 -20 C80 110 34 210 78 340 C116 450 67 575 100 730" />
                    <path d="M-20 120 C100 150 182 107 450 161" />
                    <path d="M-10 285 C112 247 218 319 450 271" />
                    <path d="M-10 476 C125 425 250 489 450 440" />
                    <path d="M164 -10 C182 155 163 328 204 720" />
                    <path d="M313 -10 C284 142 338 298 309 720" />
                    <path className="secondary" d="M95 66 L372 602M33 390 L408 85M52 585 L378 356" />
                    <path className="secondary" d="M130 0 L113 700M240 0 L264 700M390 0 L362 700" />
                  </svg>
                  <span className="map-district cartuja">CARTUJA</span>
                  <span className="map-district centro">CENTRO</span>
                  <span className="map-district zaidin">ZAIDÍN</span>
                  <span className="map-park park-one">Parque<br />Almunia</span>
                  <span className="map-park park-two">Fuentenueva</span>

                  <button className={`campus-pin etsiit ${selectedCampus === "etsiit" ? "selected" : ""}`} onClick={() => setSelectedCampus("etsiit")} type="button">
                    <span><Icon name="building" size={20} /></span>
                    <strong>ETSIIT</strong>
                  </button>
                  <button className={`campus-pin sciences ${selectedCampus === "sciences" ? "selected" : ""}`} onClick={() => setSelectedCampus("sciences")} type="button">
                    <span><Icon name="building" size={18} /></span>
                    <strong>Ciencias</strong>
                  </button>
                  <button className={`campus-pin cartuja-pin ${selectedCampus === "cartuja" ? "selected" : ""}`} onClick={() => setSelectedCampus("cartuja")} type="button">
                    <span><Icon name="building" size={18} /></span>
                    <strong>Cartuja</strong>
                  </button>

                  <span className="user-location"><i /><Icon name="location" size={20} /></span>
                  <button className="gps-button" onClick={() => {
                    setSelectedCampus(null);
                    showToast("Mapa centrado en tu ubicación");
                  }} type="button" aria-label="Centrar en mi ubicación"><Icon name="location" size={20} /></button>
                  <div className="map-legend"><span><i /> Tu ubicación</span><span><i /> Centros UGR</span></div>
                </div>

                {selectedCampus && (
                  <div className="campus-sheet">
                    <div className="campus-sheet-handle" />
                    <button className="campus-sheet-close" onClick={() => setSelectedCampus(null)} type="button" aria-label="Cerrar">×</button>
                    <div className="campus-sheet-top">
                      <span><Icon name="building" /></span>
                      <div><small>{currentCampus.campus}</small><h2>{currentCampus.name}</h2><p>{currentCampus.full}</p></div>
                    </div>
                    <div className="campus-meta">
                      <span><Icon name="location" size={15} /> {currentCampus.distance}</span>
                      <span><Icon name="clock" size={15} /> {currentCampus.hours}</span>
                    </div>
                    <button onClick={() => {
                      setPlanZoom(1);
                      setMapMode("indoor");
                    }} type="button">
                      Ver plano del edificio <Icon name="arrow" size={18} />
                    </button>
                  </div>
                )}
              </section>
            ) : (
              <section className="indoor-map">
                <div className="indoor-map-head">
                  <button onClick={() => setMapMode("city")} type="button" aria-label="Volver al mapa de Granada">‹</button>
                  <div><span>Plano interior</span><strong>{currentCampus.name}</strong></div>
                  <button aria-expanded={indoorSearchOpen} onClick={() => {
                    setIndoorSearchOpen(!indoorSearchOpen);
                    setIndoorQuery("");
                    setPoiExpanded(true);
                  }} type="button" aria-label="Buscar espacio"><Icon name="search" size={18} /></button>
                </div>
                {indoorSearchOpen && (
                  <div className="indoor-search inbox-search">
                    <Icon name="search" size={18} />
                    <input
                      aria-label="Buscar espacio"
                      autoFocus
                      onChange={(event) => setIndoorQuery(event.target.value)}
                      onKeyDown={(event) => {
                        if (event.key === "Enter" && indoorQuery.trim()) openRoute(indoorQuery.trim());
                      }}
                      placeholder="Aula, laboratorio o servicio"
                      type="search"
                      value={indoorQuery}
                    />
                  </div>
                )}
                <div className="floor-selector">
                  {["P2", "P1", "PB", "S1"].map((floor) => (
                    <button className={campusFloor === floor ? "active" : ""} key={floor} onClick={() => setCampusFloor(floor)} type="button">{floor}</button>
                  ))}
                </div>
                <div className="building-plan">
                  <span className="building-name">PLANTA {campusFloor}</span>
                  <div className="plan-layer" style={{ transform: `scale(${planZoom})` }}>
                    {floorRooms[campusFloor].map((room, index) => (
                      <button className={`campus-room cr${index + 1}`} key={room} onClick={() => openRoute(room.replace("\n", " "))} type="button">{room}</button>
                    ))}
                    <div className="campus-hall ch1" />
                    <div className="campus-hall ch2" />
                    <span className="campus-stairs">Escaleras</span>
                    <span className="campus-elevator">Ascensor</span>
                    {campusFloor === "PB" && <span className="indoor-user"><Icon name="location" size={18} /></span>}
                  </div>
                  <div className="plan-zoom">
                    <button aria-label="Acercar" disabled={planZoom >= 1.8} onClick={() => setPlanZoom(Math.min(1.8, planZoom + 0.2))} type="button">+</button>
                    <button aria-label="Alejar" disabled={planZoom <= 1} onClick={() => setPlanZoom(Math.max(1, planZoom - 0.2))} type="button">−</button>
                  </div>
                </div>
                <div className={`indoor-sheet ${poiExpanded ? "" : "collapsed"}`}>
                  <div className="poi-heading">
                    <div><strong>Puntos de interés</strong><small>Encuentra servicios dentro de {currentCampus.name === "Ciencias" ? "la Facultad de Ciencias" : currentCampus.name === "Cartuja" ? "el campus de Cartuja" : "la ETSIIT"}</small></div>
                    <button onClick={() => setPoiExpanded(!poiExpanded)} type="button" aria-expanded={poiExpanded}>
                      <span>Planta {campusFloor}</span>
                      <i className={poiExpanded ? "open" : ""}>⌄</i>
                    </button>
                  </div>
                  {poiExpanded && <div className="poi-chips">
                    {pointsOfInterest
                      .filter((poi) => poi.name.toLowerCase().includes(indoorQuery.toLowerCase()))
                      .map((poi) => (
                        <button key={poi.name} onClick={() => openRoute(poi.route)} type="button"><Icon name={poi.icon} size={17} /><span>{poi.name}</span><small>{poi.detail}</small></button>
                      ))}
                    {indoorQuery.trim() && (
                      <button onClick={() => openRoute(indoorQuery.trim())} type="button"><Icon name="arrow" size={17} /><span>Ir a «{indoorQuery.trim()}»</span><small>Buscar ruta</small></button>
                    )}
                  </div>}
                </div>
              </section>
            )}
          </main>
        ) : activeView === "profile" ? (
          <main className="student-profile-content">
            <section className="student-profile-head">
              <button onClick={() => setActiveView("home")} type="button" aria-label="Volver">‹</button>
              <span>Mi perfil</span>
              <button onClick={() => {
                setProfileDraft(profile);
                setEditingProfile(!editingProfile);
              }} type="button" aria-label={editingProfile ? "Cancelar edición" : "Editar perfil"}>{editingProfile ? "Cancelar" : "Editar"}</button>
            </section>
            <section className="student-identity">
              <span>LM</span>
              <h1>Lucía Martín García</h1>
              <p>Estudiante de Grado</p>
              <strong>Universidad de Granada</strong>
            </section>
            <section className="student-data">
              <h2>Información académica</h2>
              <div><span><Icon name="person" size={18} /></span><p><small>CORREO INSTITUCIONAL</small><strong>lucia@correo.ugr.es</strong></p></div>
              <div><span><Icon name="book" size={18} /></span><p><small>TITULACIÓN</small><strong>Grado en Ingeniería Informática</strong></p></div>
              <div><span><Icon name="building" size={18} /></span><p><small>CENTRO</small><strong>ETSIIT · Campus Aynadamar</strong></p></div>
              <div><span><Icon name="calendar" size={18} /></span><p><small>CURSO ACADÉMICO</small><strong>3º · Curso 2024/2025</strong></p></div>
            </section>
            {editingProfile ? (
              <form className="event-form profile-form" onSubmit={(event) => {
                event.preventDefault();
                setProfile({ ...profileDraft, preferredName: profileDraft.preferredName.trim() || "Lucía" });
                setEditingProfile(false);
                showToast("Perfil actualizado");
              }}>
                <div><strong>Datos de contacto</strong><button onClick={() => setEditingProfile(false)} type="button" aria-label="Cerrar">×</button></div>
                <label>
                  <span>Nombre preferido</span>
                  <input onChange={(event) => setProfileDraft({ ...profileDraft, preferredName: event.target.value })} value={profileDraft.preferredName} />
                </label>
                <label>
                  <span>Teléfono</span>
                  <input onChange={(event) => setProfileDraft({ ...profileDraft, phone: event.target.value })} type="tel" value={profileDraft.phone} />
                </label>
                <label>
                  <span>Correo personal</span>
                  <input onChange={(event) => setProfileDraft({ ...profileDraft, personalEmail: event.target.value })} type="email" value={profileDraft.personalEmail} />
                </label>
                <button type="submit">Guardar cambios</button>
              </form>
            ) : (
              <section className="student-data">
                <h2>Datos de contacto</h2>
                <div><span><Icon name="person" size={18} /></span><p><small>NOMBRE PREFERIDO</small><strong>{profile.preferredName}</strong></p></div>
                <div><span><Icon name="services" size={18} /></span><p><small>TELÉFONO</small><strong>{profile.phone || "—"}</strong></p></div>
                <div><span><Icon name="book" size={18} /></span><p><small>CORREO PERSONAL</small><strong>{profile.personalEmail || "—"}</strong></p></div>
              </section>
            )}
            <section className="profile-stats">
              <div><strong>60</strong><span>Créditos matriculados</span></div>
              <div><strong>7,8</strong><span>Nota media</span></div>
              <div><strong>78%</strong><span>Progreso del grado</span></div>
            </section>
            <button className="profile-logout" onClick={() => {
              setActiveView("home");
              setAuthenticated(false);
            }} type="button">Cerrar sesión</button>
          </main>
        ) : activeView === "more" ? (
          <main className="more-content">
            <section className="more-heading">
              <span>SERVICIOS UGR</span>
              <h1>Más</h1>
              <p>Todo lo que necesitas para tu día a día universitario.</p>
            </section>
            {nextDelivery ? (
              <button className="more-highlight" onClick={() => openEvent(nextDelivery, "more")} type="button">
                <span><Icon name="calendar" /></span>
                <div><small>PRÓXIMA ENTREGA</small><strong>{nextDelivery.title}</strong><p>{formatDate(nextDelivery.date)} · {nextDelivery.time}</p></div>
                <Icon name="chevron" size={18} />
              </button>
            ) : (
              <button className="more-highlight" onClick={() => setActiveView("calendar")} type="button">
                <span><Icon name="calendar" /></span>
                <div><small>PRÓXIMA ENTREGA</small><strong>No tienes entregas pendientes</strong><p>Añádelas desde el calendario</p></div>
                <Icon name="chevron" size={18} />
              </button>
            )}
            <section className="services-grid">
              <button onClick={() => setActiveView("grades")} type="button"><span className="service-icon grades"><Icon name="book" /></span><strong>Mis notas</strong><small>Calificaciones y expediente</small><Icon name="chevron" size={16} /></button>
              <button onClick={goToMessages} type="button"><span className="service-icon messages"><Icon name="services" /></span><strong>Mis mensajes</strong><small>{unreadCount ? `${unreadCount} ${unreadCount === 1 ? "mensaje sin leer" : "mensajes sin leer"}` : "Todo leído"}</small>{unreadCount > 0 ? <i>{unreadCount}</i> : <Icon name="chevron" size={16} />}</button>
              <button onClick={() => setActiveView("calendar")} type="button"><span className="service-icon calendar"><Icon name="calendar" /></span><strong>Calendario</strong><small>Entregas y exámenes</small><Icon name="chevron" size={16} /></button>
              <button onClick={goToHelp} type="button"><span className="service-icon help"><Icon name="location" /></span><strong>Ayuda y soporte</strong><small>Incidencias y contacto</small><Icon name="chevron" size={16} /></button>
            </section>
            <section className="more-settings">
              <button onClick={() => {
                setEditingProfile(false);
                setActiveView("profile");
              }} type="button"><Icon name="person" size={19} /><span><strong>Cuenta y preferencias</strong><small>Datos personales y configuración</small></span><Icon name="chevron" size={17} /></button>
              <button onClick={() => setActiveView("accessibility")} type="button"><Icon name="wheelchair" size={19} /><span><strong>Accesibilidad</strong><small>Preferencias de navegación</small></span><Icon name="chevron" size={17} /></button>
            </section>
          </main>
        ) : activeView === "grades" ? (
          <main className="utility-content">
            <section className="utility-head">
              <button onClick={() => setActiveView("more")} type="button">‹</button>
              <div><span>EXPEDIENTE ACADÉMICO</span><h1>Mis notas</h1></div>
              <button onClick={downloadTranscript} type="button" aria-label="Descargar expediente">↓</button>
            </section>
            <section className="grade-summary">
              <div><span>7,8</span><small>Nota media</small></div>
              <div><span>156</span><small>Créditos superados</small></div>
              <div><span>13</span><small>Asignaturas</small></div>
            </section>
            <div className="course-tabs">
              {(Object.keys(grades) as Array<keyof typeof grades>).map((course) => (
                <button className={gradeCourse === course ? "active" : ""} key={course} onClick={() => setGradeCourse(course)} type="button">{course} curso</button>
              ))}
            </div>
            <section className="grade-list">
              <div className="grade-list-head"><h2>Asignaturas de {gradeCourse}</h2><span>{grades[gradeCourse].length} superadas</span></div>
              {grades[gradeCourse].map(([subject, grade]) => (
                <div className="grade-row" key={subject}>
                  <span className="grade-check">✓</span>
                  <div><strong>{subject}</strong><small>6 ECTS · Superada</small></div>
                  <span className="grade-value">{grade}</span>
                </div>
              ))}
            </section>
          </main>
        ) : activeView === "calendar" ? (
          <main className="utility-content calendar-content">
            <section className="utility-head">
              <button onClick={() => setActiveView("more")} type="button">‹</button>
              <div><span>{monthLabel.toUpperCase()}</span><h1>Calendario</h1></div>
              <button onClick={() => (showEventForm ? setShowEventForm(false) : startNewEvent())} type="button" aria-label="Añadir evento">+</button>
            </section>
            <section className="month-calendar">
              <div className="calendar-month"><button onClick={() => shiftMonth(-1)} type="button" aria-label="Mes anterior">‹</button><strong>{monthLabel}</strong><button onClick={() => shiftMonth(1)} type="button" aria-label="Mes siguiente">›</button></div>
              <div className="week-labels">{["L", "M", "X", "J", "V", "S", "D"].map((day) => <span key={day}>{day}</span>)}</div>
              <div className="month-grid">
                {monthCells.map((day, index) => {
                  if (!day) return <span key={index} />;
                  const date = `${calendarMonth}-${String(day).padStart(2, "0")}`;
                  const events = calendarEvents.filter((event) => event.date === date);
                  return (
                    <button
                      aria-label={formatDate(date)}
                      aria-pressed={date === selectedDate}
                      className={`${date === TODAY ? "today" : ""} ${date === selectedDate ? "selected" : ""}`}
                      key={index}
                      onClick={() => selectDate(date)}
                      type="button"
                    >
                      {day}
                      {events.length > 0 && <i className={events.some((event) => event.type === "Examen") ? "exam" : ""} />}
                    </button>
                  );
                })}
              </div>
            </section>

            <section className="upcoming-events selected-day">
              <div>
                <h2>{formatDate(selectedDate)}</h2>
                {!showEventForm && <button className="add-event-inline" onClick={startNewEvent} type="button">+ Añadir</button>}
              </div>
              {dayEvents.length ? dayEvents.map(renderEvent) : <p className="empty-day">No tienes entregas ni exámenes este día.</p>}
            </section>

            {showEventForm && !editingEventId && eventForm}

            <section className="upcoming-events">
              <div><h2>Próximos eventos</h2><span>{calendarEvents.length}</span></div>
              {sortedEvents.map(renderEvent)}
            </section>
          </main>
        ) : activeView === "event" && selectedEvent ? (
          <main className="utility-content">
            <section className="utility-head">
              <button onClick={() => {
                setEditingEventId(null);
                setConfirmDelete(false);
                setActiveView(eventBackView);
              }} type="button" aria-label="Volver">‹</button>
              <div><span>{selectedEvent.type.toUpperCase()}</span><h1>{editingEventId ? "Editar" : "Detalle"}</h1></div>
              <span />
            </section>
            {editingEventId === selectedEvent.id ? eventForm : (<>
            <section className={`event-hero ${selectedEvent.type === "Examen" ? "exam" : ""}`}>
              <span>{selectedEvent.type}</span>
              <h2>{selectedEvent.title}</h2>
              <p>{selectedEvent.subject}</p>
              <strong><Icon name="clock" size={15} /> {formatCountdown(selectedEvent.date)}</strong>
            </section>
            <section className="student-data">
              <h2>Información</h2>
              <div><span><Icon name="calendar" size={18} /></span><p><small>FECHA</small><strong>{formatDate(selectedEvent.date)}</strong></p></div>
              <div><span><Icon name="clock" size={18} /></span><p><small>{selectedEvent.type === "Examen" ? "HORA DEL EXAMEN" : "HORA DE ENTREGA"}</small><strong>{formatEventTime(selectedEvent)}</strong></p></div>
              <div><span><Icon name="book" size={18} /></span><p><small>ASIGNATURA</small><strong>{selectedEvent.subject}</strong></p></div>
            </section>
            <section className="student-data event-detail-description">
              <h2>Descripción</h2>
              <p>{selectedEvent.description || "Sin descripción."}</p>
            </section>
            <button className="profile-logout event-calendar-link" onClick={() => {
              selectDate(selectedEvent.date);
              setActiveView("calendar");
            }} type="button">Ver en el calendario</button>
            {confirmDelete ? (
              <div className="delete-confirm">
                <p>¿Eliminar «{selectedEvent.title}»? Esta acción no se puede deshacer.</p>
                <div>
                  <button onClick={() => setConfirmDelete(false)} type="button">Cancelar</button>
                  <button className="danger" onClick={() => deleteEvent(selectedEvent.id)} type="button">Eliminar</button>
                </div>
              </div>
            ) : (
              <div className="event-actions">
                <button onClick={() => startEditEvent(selectedEvent)} type="button">Editar</button>
                <button className="danger" onClick={() => setConfirmDelete(true)} type="button">Eliminar</button>
              </div>
            )}
            </>)}
          </main>
        ) : activeView === "accessibility" ? (
          <main className="utility-content">
            <section className="utility-head">
              <button onClick={() => setActiveView("more")} type="button" aria-label="Volver">‹</button>
              <div><span>PREFERENCIAS</span><h1>Accesibilidad</h1></div>
              <span />
            </section>
            <section className="settings-list">
              {[
                { icon: "wheelchair" as IconName, title: "Ruta accesible por defecto", detail: "Evitar escaleras y usar ascensores y rampas", value: accessible, set: setAccessible },
                { icon: "search" as IconName, title: "Texto más grande", detail: "Aumenta el tamaño de textos y botones", value: largeText, set: setLargeText },
                { icon: "services" as IconName, title: "Alto contraste", detail: "Refuerza colores y bordes", value: highContrast, set: setHighContrast },
                { icon: "clock" as IconName, title: "Reducir animaciones", detail: "Desactiva transiciones y efectos de movimiento", value: reduceMotion, set: setReduceMotion },
              ].map((option) => (
                <div className="access-card" key={option.title}>
                  <span><Icon name={option.icon} /></span>
                  <div><strong>{option.title}</strong><small>{option.detail}</small></div>
                  <button aria-checked={option.value} aria-label={option.title} className={option.value ? "on" : ""} onClick={() => option.set(!option.value)} role="switch" type="button"><i /></button>
                </div>
              ))}
            </section>
            <p className="settings-note">Estas preferencias se aplican al momento en toda la app.</p>
          </main>
        ) : activeView === "messages" && mailMode === "compose" ? (
          <main className="utility-content">
            <section className="utility-head">
              <button onClick={() => setMailMode("list")} type="button" aria-label="Cancelar">‹</button>
              <div><span>CORREO INSTITUCIONAL</span><h1>{composeSubject.startsWith("Re:") ? "Responder" : "Nuevo mensaje"}</h1></div>
              <span />
            </section>
            <form className="event-form mail-compose" onSubmit={sendMail}>
              <div><strong>De: lucia@correo.ugr.es</strong><button onClick={() => setMailMode("list")} type="button" aria-label="Descartar">×</button></div>
              <label>
                <span>Para</span>
                <input list="mail-contacts" onChange={(event) => setComposeTo(event.target.value)} placeholder="Nombre o correo @ugr.es" required value={composeTo} />
                <datalist id="mail-contacts">
                  {contacts.map((contact) => <option key={contact.email} value={contact.email}>{contact.name}</option>)}
                </datalist>
              </label>
              <label>
                <span>Asunto</span>
                <input onChange={(event) => setComposeSubject(event.target.value)} placeholder="Asunto del mensaje" value={composeSubject} />
              </label>
              <label>
                <span>Mensaje</span>
                <textarea onChange={(event) => setComposeBody(event.target.value)} placeholder="Escribe tu mensaje..." required rows={9} value={composeBody} />
              </label>
              <button type="submit">Enviar mensaje</button>
            </form>
          </main>
        ) : activeView === "messages" && mailMode === "read" && openMail ? (
          <main className="utility-content">
            <section className="utility-head">
              <button onClick={() => setMailMode("list")} type="button" aria-label="Volver">‹</button>
              <div><span>{mailFolders.find((folder) => folder.key === openMail.folder)!.label.toUpperCase()}</span><h1>Mensaje</h1></div>
              <span />
            </section>
            <section className="mail-reader">
              <h2>{openMail.subject}</h2>
              <div className="mail-reader-from">
                <span className="sender-avatar">{initialsOf(openMail.outgoing ? "Lucía Martín" : openMail.name)}</span>
                <div>
                  <strong>{openMail.outgoing ? "Lucía Martín" : openMail.name}</strong>
                  <small>{openMail.outgoing ? `Para: ${openMail.name} <${openMail.email}>` : `${openMail.email} · Para: mí`}</small>
                </div>
                <small>{openMail.time}</small>
              </div>
              <p className="mail-body">{openMail.body}</p>
            </section>
            {confirmMailDelete ? (
              <div className="delete-confirm">
                <p>¿Eliminar este mensaje? Esta acción no se puede deshacer.</p>
                <div>
                  <button onClick={() => setConfirmMailDelete(false)} type="button">Cancelar</button>
                  <button className="danger" onClick={() => deleteMail(openMail)} type="button">Eliminar</button>
                </div>
              </div>
            ) : (
              <div className="event-actions mail-actions">
                {!openMail.outgoing && (
                  <button className="primary" onClick={() => startCompose(openMail.email, openMail.subject.startsWith("Re:") ? openMail.subject : `Re: ${openMail.subject}`)} type="button">Responder</button>
                )}
                {openMail.folder === "archived" ? (
                  <button onClick={() => moveMail(openMail, openMail.outgoing ? "sent" : "inbox")} type="button">Desarchivar</button>
                ) : (
                  <button onClick={() => moveMail(openMail, "archived")} type="button">Archivar</button>
                )}
                <button className="danger" onClick={() => setConfirmMailDelete(true)} type="button">Eliminar</button>
              </div>
            )}
          </main>
        ) : activeView === "messages" ? (
          <main className="utility-content">
            <section className="utility-head">
              <button onClick={() => setActiveView("more")} type="button" aria-label="Volver">‹</button>
              <div><span>CORREO INSTITUCIONAL</span><h1>Mis mensajes</h1></div>
              <button onClick={() => startCompose()} type="button" aria-label="Redactar mensaje">+</button>
            </section>
            {mailNotice && <div className="mail-notice"><span>✓</span>{mailNotice}<button onClick={() => setMailNotice(null)} type="button" aria-label="Cerrar aviso">×</button></div>}
            <div className="inbox-search">
              <Icon name="search" size={18} />
              <input aria-label="Buscar en el correo" onChange={(event) => setMailQuery(event.target.value)} placeholder="Buscar en el correo" type="search" value={mailQuery} />
            </div>
            <div className="inbox-tabs" role="tablist">
              {mailFolders.map((folder) => (
                <button
                  aria-selected={mailFolder === folder.key}
                  className={mailFolder === folder.key ? "active" : ""}
                  key={folder.key}
                  onClick={() => {
                    setMailFolder(folder.key);
                    setMailNotice(null);
                  }}
                  role="tab"
                  type="button"
                >
                  {folder.label}
                  {folder.key === "inbox" && unreadCount > 0 && <span>{unreadCount}</span>}
                </button>
              ))}
            </div>
            <section className="message-list">
              {folderMails.map((mail) => (
                <button className={mail.unread ? "unread" : ""} key={mail.id} onClick={() => readMail(mail)} type="button">
                  <span className="sender-avatar">{initialsOf(mail.name)}</span>
                  <div><span><strong>{mail.outgoing ? `Para: ${mail.name}` : mail.name}</strong><small>{mail.time}</small></span><h2>{mail.subject}</h2><p>{mail.body.replace(/\s+/g, " ")}</p></div>
                  {mail.unread && <i />}
                </button>
              ))}
              {folderMails.length === 0 && (
                <p className="mail-empty">
                  {mailQuery ? "No hay mensajes que coincidan con tu búsqueda." : mailFolder === "archived" ? "No tienes mensajes archivados." : mailFolder === "sent" ? "Aún no has enviado ningún mensaje." : "Tu bandeja de entrada está vacía."}
                </p>
              )}
            </section>
          </main>
        ) : (
          helpMode === "report" ? (
            <main className="utility-content">
              <section className="utility-head">
                <button onClick={() => setHelpMode("home")} type="button" aria-label="Cancelar">‹</button>
                <div><span>CENTRO DE AYUDA</span><h1>Nueva incidencia</h1></div>
                <span />
              </section>
              <form className="event-form ticket-form" onSubmit={sendTicket}>
                <div><strong>Cuéntanos qué ocurre</strong><button onClick={() => setHelpMode("home")} type="button" aria-label="Descartar">×</button></div>
                <label>
                  <span>Categoría</span>
                  <select onChange={(event) => setTicketTopic(event.target.value as HelpTopic)} value={ticketTopic}>
                    {helpTopics.map((topic) => <option key={topic.key} value={topic.key}>{topic.title}</option>)}
                  </select>
                </label>
                <label>
                  <span>Asunto</span>
                  <input onChange={(event) => setTicketSubject(event.target.value)} placeholder="Resume el problema en una frase" required value={ticketSubject} />
                </label>
                <label>
                  <span>Descripción</span>
                  <textarea onChange={(event) => setTicketDescription(event.target.value)} placeholder="¿Qué estabas haciendo? ¿Qué esperabas que pasara?" required rows={6} value={ticketDescription} />
                </label>
                <label>
                  <span>Lugar <small>(opcional)</small></span>
                  <input onChange={(event) => setTicketPlace(event.target.value)} placeholder="Ej.: ETSIIT · Planta 1, aula B-12" value={ticketPlace} />
                </label>
                <button type="submit">Enviar incidencia</button>
              </form>
            </main>
          ) : helpMode === "topic" ? (
            <main className="utility-content">
              <section className="utility-head">
                <button onClick={() => setHelpMode("home")} type="button" aria-label="Volver">‹</button>
                <div><span>AYUDA RÁPIDA</span><h1>{helpTopics.find((topic) => topic.key === helpTopic)!.title.split(" ")[0]}</h1></div>
                <span />
              </section>
              <section className="faq-list">
                <h2>{helpTopics.find((topic) => topic.key === helpTopic)!.title}</h2>
                {faqs.filter((faq) => faq.topic === helpTopic).map(renderFaq)}
              </section>
              <section className="report-card">
                <div><span>¿No has encontrado la respuesta?</span><h2>Comunícanos el problema</h2><p>Crearemos una incidencia y te responderemos por correo.</p></div>
                <button onClick={() => startTicket(helpTopic)} type="button">Crear incidencia <Icon name="arrow" size={17} /></button>
              </section>
            </main>
          ) : (
            <main className="utility-content">
              <section className="utility-head">
                <button onClick={() => setActiveView("more")} type="button" aria-label="Volver">‹</button>
                <div><span>CENTRO DE AYUDA</span><h1>Ayuda y soporte</h1></div>
                <span />
              </section>
              {helpNotice && <div className="mail-notice"><span>✓</span>{helpNotice}<button onClick={() => setHelpNotice(null)} type="button" aria-label="Cerrar aviso">×</button></div>}
              <section className="help-hero">
                <span><Icon name="services" /></span>
                <h2>¿En qué podemos ayudarte?</h2>
                <p>Consulta preguntas frecuentes o comunícanos una incidencia.</p>
                <div><Icon name="search" size={18} /><input aria-label="Buscar una pregunta" onChange={(event) => setHelpQuery(event.target.value)} placeholder="Buscar una pregunta" type="search" value={helpQuery} /></div>
              </section>
              {helpQuery.trim() ? (
                <section className="faq-list">
                  <h2>Resultados</h2>
                  {faqs.filter((faq) => `${faq.question} ${faq.answer}`.toLowerCase().includes(helpQuery.trim().toLowerCase())).map(renderFaq)}
                  {!faqs.some((faq) => `${faq.question} ${faq.answer}`.toLowerCase().includes(helpQuery.trim().toLowerCase())) && (
                    <p className="mail-empty">No hemos encontrado preguntas sobre «{helpQuery.trim()}». Prueba con otras palabras o crea una incidencia.</p>
                  )}
                </section>
              ) : (
                <section className="help-options">
                  <h2>Ayuda rápida</h2>
                  {helpTopics.map((topic) => (
                    <button key={topic.key} onClick={() => {
                      setHelpTopic(topic.key);
                      setOpenFaq(null);
                      setHelpMode("topic");
                    }} type="button"><span><Icon name={topic.icon} /></span><div><strong>{topic.title}</strong><small>{topic.detail}</small></div><Icon name="chevron" size={17} /></button>
                  ))}
                </section>
              )}
              <section className="report-card">
                <div><span>¿Algo no funciona?</span><h2>Comunicar una incidencia</h2><p>Envíanos una descripción y nuestro equipo técnico la revisará.</p></div>
                <button onClick={() => startTicket()} type="button">Crear incidencia <Icon name="arrow" size={17} /></button>
              </section>
              {tickets.length > 0 && (
                <section className="ticket-list">
                  <h2>Mis incidencias</h2>
                  {tickets.map((ticket) => (
                    <article key={ticket.id}>
                      <div><small>{ticket.id} · {formatDate(ticket.date)}</small><strong>{ticket.subject}</strong>{ticket.place && <p>{ticket.place}</p>}</div>
                      <span className={`ticket-status ${ticket.status === "Resuelta" ? "done" : ""}`}>{ticket.status}</span>
                    </article>
                  ))}
                </section>
              )}
              <section className="contact-support">
                <span>Soporte UGR</span>
                <strong>958 24 10 00</strong>
                <small>Lunes a viernes · 08:00–20:00</small>
                <div>
                  <a href="tel:+34958241000">Llamar</a>
                  <a href="mailto:soporte@ugr.es?subject=Ayuda%20UGR%20Campus">soporte@ugr.es</a>
                </div>
              </section>
            </main>
          )
        )}

        <nav className="bottom-nav" aria-label="Navegación principal">
          <button className={activeView === "home" ? "active" : ""} onClick={() => setActiveView("home")} type="button"><Icon name="home" /><span>Inicio</span></button>
          <button className={activeView === "map" ? "active" : ""} onClick={() => {
            setMapMode("city");
            setActiveView("map");
          }} type="button"><Icon name="map" /><span>Mapa</span></button>
          <button className="nav-route" type="button" onClick={() => openRoute()}>
            <span><Icon name="location" /></span>
            <small>Ruta</small>
          </button>
          <button className={activeView === "schedule" ? "active" : ""} onClick={() => setActiveView("schedule")} type="button"><Icon name="calendar" /><span>Horario</span></button>
          <button className={["more", "grades", "calendar", "event", "messages", "help", "accessibility"].includes(activeView) ? "active more-nav-button" : "more-nav-button"} onClick={() => setActiveView("more")} type="button"><Icon name="menu" /><span>Más</span>{unreadCount > 0 && <i>{unreadCount}</i>}</button>
        </nav>

        {showRoute && (
          <section className="route-screen">
            <div className="route-map">
              <div className="map-head">
                <button onClick={() => {
                  setNavStep(null);
                  setShowRoute(false);
                }} type="button" aria-label="Volver">‹</button>
                <div>
                  <span>{navStep === null ? "Tu ruta" : "Navegando"}</span>
                  <strong>{destination}</strong>
                </div>
                <button aria-expanded={routeMenu} onClick={() => setRouteMenu(!routeMenu)} type="button" aria-label="Más opciones">•••</button>
              </div>
              {routeMenu && (
                <div className="pop-menu route-menu">
                  <button onClick={() => {
                    setRouteMenu(false);
                    setShowRoute(false);
                    setNavStep(null);
                    setMapMode("indoor");
                    setActiveView("map");
                  }} type="button"><Icon name="map" size={17} /> Ver plano del edificio</button>
                  <button onClick={shareRoute} type="button"><Icon name="arrow" size={17} /> Compartir ruta</button>
                  <button className="danger" onClick={() => {
                    setRouteMenu(false);
                    setNavStep(null);
                    setShowRoute(false);
                  }} type="button"><Icon name="location" size={17} /> Cancelar ruta</button>
                </div>
              )}

              <div className="floor-label">PLANTA 1</div>
              <div className="room r1">B-10</div>
              <div className="room r2">B-11</div>
              <div className="room r3 active">B-12</div>
              <div className="room r4">B-13</div>
              <div className="room r5">Biblioteca</div>
              <div className="hall h1" />
              <div className="hall h2" />
              <svg className="route-path" viewBox="0 0 360 520" preserveAspectRatio="none">
                <path d="M83 438 L180 438 L180 287 L267 287 L267 144" />
              </svg>
              <span className="start-pin"><Icon name="location" size={21} /></span>
              <span className="end-pin"><Icon name="pin" size={22} /></span>
            </div>

            <div className="route-sheet">
              <div className="sheet-handle" />
              {navStep === null ? (<>
              <div className="route-summary">
                <div><span>{accessible ? "5" : "3"}</span><small>min</small></div>
                <div><span>{accessible ? "210" : "140"}</span><small>metros</small></div>
                <div><span>1</span><small>planta</small></div>
              </div>
              <div className="next-step">
                <span className="turn-icon">↑</span>
                <div><small>PRIMER PASO</small><strong>Continúa recto por el pasillo</strong></div>
                <span>32 m</span>
              </div>
              <div className="access-card">
                <span><Icon name="wheelchair" /></span>
                <div>
                  <strong>Ruta accesible</strong>
                  <small>Evitar escaleras y usar ascensores</small>
                </div>
                <button
                  aria-checked={accessible}
                  aria-label="Activar ruta accesible"
                  className={accessible ? "on" : ""}
                  onClick={() => setAccessible(!accessible)}
                  role="switch"
                  type="button"
                >
                  <i />
                </button>
              </div>
              <button className="start-button" onClick={() => setNavStep(0)} type="button">
                Iniciar navegación
                <Icon name="arrow" size={20} />
              </button>
              {accessible && <p className="accessible-note"><Icon name="wheelchair" size={16} /> Ruta adaptada mediante ascensor</p>}
              </>) : (<>
                <div className="nav-progress">
                  <span>Paso {navStep + 1} de {routeSteps.length}</span>
                  <div><i style={{ width: `${((navStep + 1) / routeSteps.length) * 100}%` }} /></div>
                </div>
                <div className="next-step">
                  <span className="turn-icon">{routeSteps[navStep].icon}</span>
                  <div><small>{navStep === routeSteps.length - 1 ? "DESTINO" : "AHORA"}</small><strong>{routeSteps[navStep].text}</strong></div>
                  <span>{routeSteps[navStep].distance}</span>
                </div>
                {navStep < routeSteps.length - 1 && <p className="nav-then">Después: {routeSteps[navStep + 1].text}</p>}
                <div className="event-actions nav-controls">
                  <button disabled={navStep === 0} onClick={() => setNavStep(navStep - 1)} type="button">Anterior</button>
                  <button className="primary" onClick={() => (navStep === routeSteps.length - 1 ? finishNavigation() : setNavStep(navStep + 1))} type="button">
                    {navStep === routeSteps.length - 1 ? "Finalizar" : "Siguiente"}
                  </button>
                </div>
                <button className="nav-exit" onClick={() => setNavStep(null)} type="button">Salir de la navegación</button>
              </>)}
            </div>
          </section>
        )}

        {toast && <div className="toast" role="status">{toast}</div>}
      </div>
    </div>
  );
}