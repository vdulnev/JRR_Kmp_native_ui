/* Icons (clean line set, ~SF Symbols vibe), procedural cover art, VU meter.
   Exported to window. Load after React. */

function Icon({ name, size = 20, stroke = 1.7, fill, style, className }) {
  const p = {
    width: size, height: size, viewBox: "0 0 24 24", fill: "none",
    stroke: "currentColor", strokeWidth: stroke, strokeLinecap: "round",
    strokeLinejoin: "round", style, className,
  };
  switch (name) {
    case "disc": // now playing
      return (<svg {...p}><circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="2.4"/></svg>);
    case "library":
      return (<svg {...p}><rect x="3" y="3" width="7" height="7" rx="1.4"/><rect x="14" y="3" width="7" height="7" rx="1.4"/><rect x="3" y="14" width="7" height="7" rx="1.4"/><rect x="14" y="14" width="7" height="7" rx="1.4"/></svg>);
    case "zones":
      return (<svg {...p}><path d="M5 9v6M9 6v12M15 6v12M19 9v6"/></svg>);
    case "gear":
      return (<svg {...p}><circle cx="12" cy="12" r="3.2"/><path d="M12 2.5v2M12 19.5v2M21.5 12h-2M4.5 12h-2M18.7 5.3l-1.4 1.4M6.7 17.3l-1.4 1.4M18.7 18.7l-1.4-1.4M6.7 6.7L5.3 5.3"/></svg>);
    case "play":
      return (<svg {...p} fill={fill || "currentColor"} stroke="none"><path d="M7 5.5v13a1 1 0 0 0 1.5.87l11-6.5a1 1 0 0 0 0-1.74l-11-6.5A1 1 0 0 0 7 5.5z"/></svg>);
    case "pause":
      return (<svg {...p} fill={fill || "currentColor"} stroke="none"><rect x="6" y="5" width="4" height="14" rx="1"/><rect x="14" y="5" width="4" height="14" rx="1"/></svg>);
    case "prev":
      return (<svg {...p} fill={fill || "currentColor"} stroke="none"><rect x="5" y="5" width="2.4" height="14" rx="1"/><path d="M19 6.2v11.6a.8.8 0 0 1-1.25.66l-8.5-5.8a.8.8 0 0 1 0-1.32l8.5-5.8A.8.8 0 0 1 19 6.2z"/></svg>);
    case "next":
      return (<svg {...p} fill={fill || "currentColor"} stroke="none"><rect x="16.6" y="5" width="2.4" height="14" rx="1"/><path d="M5 6.2v11.6a.8.8 0 0 0 1.25.66l8.5-5.8a.8.8 0 0 0 0-1.32l-8.5-5.8A.8.8 0 0 0 5 6.2z"/></svg>);
    case "shuffle":
      return (<svg {...p}><path d="M16 4h4v4M4 20l16-16M4 4l5 5M20 16v4h-4M14.5 14.5L20 20"/></svg>);
    case "repeat":
      return (<svg {...p}><path d="M17 2l3 3-3 3M7 22l-3-3 3-3"/><path d="M20 5H8a4 4 0 0 0-4 4v1M4 19h12a4 4 0 0 0 4-4v-1"/></svg>);
    case "repeat1":
      return (<svg {...p}><path d="M17 2l3 3-3 3M7 22l-3-3 3-3"/><path d="M20 5H8a4 4 0 0 0-4 4v1M4 19h12a4 4 0 0 0 4-4v-1"/><path d="M11.6 10.5l1.4-.8v4.3" strokeWidth="1.4"/></svg>);
    case "list":
      return (<svg {...p}><path d="M8 6h12M8 12h12M8 18h12M3.5 6h.01M3.5 12h.01M3.5 18h.01"/></svg>);
    case "speaker":
      return (<svg {...p}><path d="M11 5L6 9H3v6h3l5 4V5z"/><path d="M15.5 8.5a5 5 0 0 1 0 7M18.5 6a8 8 0 0 1 0 12"/></svg>);
    case "speaker-mute":
      return (<svg {...p}><path d="M11 5L6 9H3v6h3l5 4V5z"/><path d="M22 9l-6 6M16 9l6 6"/></svg>);
    case "star":
      return (<svg {...p}><path d="M12 3.5l2.6 5.27 5.82.85-4.21 4.1.99 5.79L12 16.77l-5.2 2.73.99-5.79-4.21-4.1 5.82-.85L12 3.5z"/></svg>);
    case "star-fill":
      return (<svg {...p} fill={fill || "currentColor"} stroke={fill || "currentColor"}><path d="M12 3.5l2.6 5.27 5.82.85-4.21 4.1.99 5.79L12 16.77l-5.2 2.73.99-5.79-4.21-4.1 5.82-.85L12 3.5z"/></svg>);
    case "search":
      return (<svg {...p}><circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/></svg>);
    case "x":
      return (<svg {...p}><path d="M6 6l12 12M18 6L6 18"/></svg>);
    case "x-circle":
      return (<svg {...p}><circle cx="12" cy="12" r="9"/><path d="M9 9l6 6M15 9l-6 6"/></svg>);
    case "chevron-left":
      return (<svg {...p}><path d="M15 5l-7 7 7 7"/></svg>);
    case "chevron-right":
      return (<svg {...p}><path d="M9 5l7 7-7 7"/></svg>);
    case "ellipsis":
      return (<svg {...p}><circle cx="5" cy="12" r="1.5" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.5" fill="currentColor" stroke="none"/><circle cx="19" cy="12" r="1.5" fill="currentColor" stroke="none"/></svg>);
    case "download":
      return (<svg {...p}><path d="M12 3v12M7 11l5 4 5-4M5 20h14"/></svg>);
    case "downloaded":
      return (<svg {...p}><path d="M20 6L9 17l-5-5"/></svg>);
    case "downloaded-box":
      return (<svg {...p}><rect x="3.5" y="3.5" width="17" height="17" rx="3"/><path d="M8 12l3 3 5-6" strokeWidth="1.6"/></svg>);
    case "headphones":
      return (<svg {...p}><path d="M4 14v-2a8 8 0 0 1 16 0v2"/><rect x="3" y="13" width="4" height="7" rx="1.5"/><rect x="17" y="13" width="4" height="7" rx="1.5"/></svg>);
    case "plus":
      return (<svg {...p}><path d="M12 5v14M5 12h14"/></svg>);
    case "play-next":
      return (<svg {...p}><path d="M4 6l8 6-8 6V6z"/><path d="M20 5v14"/></svg>);
    case "info":
      return (<svg {...p}><circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8h.01"/></svg>);
    case "refresh":
      return (<svg {...p}><path d="M20 11a8 8 0 1 0-2.3 5.6M20 5v6h-6"/></svg>);
    case "grip":
      return (<svg {...p}><circle cx="9" cy="6" r="1.3" fill="currentColor" stroke="none"/><circle cx="9" cy="12" r="1.3" fill="currentColor" stroke="none"/><circle cx="9" cy="18" r="1.3" fill="currentColor" stroke="none"/><circle cx="15" cy="6" r="1.3" fill="currentColor" stroke="none"/><circle cx="15" cy="12" r="1.3" fill="currentColor" stroke="none"/><circle cx="15" cy="18" r="1.3" fill="currentColor" stroke="none"/></svg>);
    case "trash":
      return (<svg {...p}><path d="M4 7h16M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2M6 7l1 13h10l1-13"/></svg>);
    case "sidebar":
      return (<svg {...p}><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16"/></svg>);
    case "car":
      return (<svg {...p}><path d="M5 11l1.5-4.5A2 2 0 0 1 8.4 5h7.2a2 2 0 0 1 1.9 1.5L19 11M5 11h14v5H5v-5z"/><circle cx="8" cy="16" r="1.2"/><circle cx="16" cy="16" r="1.2"/></svg>);
    case "monitor":
      return (<svg {...p}><rect x="3" y="4" width="18" height="12" rx="2"/><path d="M9 20h6M12 16v4"/></svg>);
    default:
      return (<svg {...p}><circle cx="12" cy="12" r="9"/></svg>);
  }
}

/* ---- Procedural cover art ----
   Deterministic, minimalist "record-cover" tiles seeded from the album name.
   Stays dark + muted to fit the palette. Five motif families. */
function hashStr(s) {
  let h = 2166136261;
  for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 16777619); }
  return Math.abs(h);
}

function CoverArt({ album, label, showLabel = false }) {
  const seed = hashStr(album.id || album.name || "x");
  const hue = (seed % 360);
  const hue2 = (hue + 40 + (seed % 30)) % 360;
  const motif = seed % 5;
  const base = `hsl(${hue}, 18%, 9%)`;
  const base2 = `hsl(${hue2}, 22%, 14%)`;
  const gold = "rgba(200,146,42,0.85)";
  const faint = "rgba(240,237,232,0.10)";
  const id = "g" + seed;

  let motifEl = null;
  if (motif === 0) {
    motifEl = (<g><line x1="0" y1="100" x2="100" y2="0" stroke={gold} strokeWidth="2.2"/><line x1="-20" y1="100" x2="80" y2="0" stroke={faint} strokeWidth="1"/></g>);
  } else if (motif === 1) {
    motifEl = (<g><circle cx="50" cy="50" r="30" stroke={gold} strokeWidth="1.6" fill="none"/><circle cx="50" cy="50" r="18" stroke={faint} strokeWidth="1" fill="none"/></g>);
  } else if (motif === 2) {
    motifEl = (<g><rect x="20" y="44" width="60" height="3" fill={gold}/><rect x="20" y="54" width="40" height="2" fill={faint}/></g>);
  } else if (motif === 3) {
    motifEl = (<g><circle cx="62" cy="38" r="22" fill={`hsl(${hue2},30%,18%)`}/><circle cx="62" cy="38" r="22" stroke={faint} strokeWidth="1" fill="none"/></g>);
  } else {
    motifEl = (<g><path d="M0 70 Q 50 30 100 70" stroke={gold} strokeWidth="1.8" fill="none"/><path d="M0 80 Q 50 45 100 80" stroke={faint} strokeWidth="1" fill="none"/></g>);
  }

  return (
    <div className="cover cover-proc">
      <svg viewBox="0 0 100 100" width="100%" height="100%" preserveAspectRatio="xMidYMid slice" style={{ display: "block" }}>
        <defs>
          <linearGradient id={id} x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stopColor={base2}/>
            <stop offset="1" stopColor={base}/>
          </linearGradient>
        </defs>
        <rect width="100" height="100" fill={`url(#${id})`}/>
        {motifEl}
      </svg>
      {showLabel && <div className="pc-label">{label || album.albumArtist}</div>}
    </div>
  );
}

function VuMeter({ playing }) {
  return (
    <span className={"vu" + (playing ? " play" : "")} aria-hidden="true">
      {[0, 1, 2, 3, 4].map((i) => <span key={i} className="bar" />)}
    </span>
  );
}

Object.assign(window, { Icon, CoverArt, VuMeter, hashStr });
