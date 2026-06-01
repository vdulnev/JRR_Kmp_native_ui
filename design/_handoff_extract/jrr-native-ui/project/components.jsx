/* Shared UI components for the large-screen prototype.
   Depends on window.Icon / CoverArt / VuMeter. Exports to window. */

const { createContext, useContext, useState, useEffect, useRef, useCallback } = React;

// ---- helpers ----
function fmtTime(totalSec) {
  totalSec = Math.max(0, Math.floor(totalSec));
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${m}:${String(s).padStart(2, "0")}`;
}
function albumDuration(album) {
  const totalMs = album.tracks.reduce((a, t) => a + t.durationMs, 0);
  const min = Math.round(totalMs / 60000);
  return `${min} MIN`;
}

// =================================================================
// Context menu system
// =================================================================
const MenuCtx = createContext(null);
function useMenu() { return useContext(MenuCtx); }

function ContextMenuHost({ menu, onClose }) {
  const ref = useRef(null);
  const [pos, setPos] = useState({ x: menu ? menu.x : 0, y: menu ? menu.y : 0 });

  useEffect(() => {
    if (!menu) return;
    // Reposition to keep within viewport
    const el = ref.current;
    if (!el) return;
    const r = el.getBoundingClientRect();
    let x = menu.x, y = menu.y;
    if (x + r.width > window.innerWidth - 8) x = window.innerWidth - r.width - 8;
    if (y + r.height > window.innerHeight - 8) y = window.innerHeight - r.height - 8;
    setPos({ x, y });
  }, [menu]);

  useEffect(() => {
    if (!menu) return;
    const onKey = (e) => { if (e.key === "Escape") onClose(); };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [menu, onClose]);

  if (!menu) return null;
  return (
    <React.Fragment>
      <div className="ctx-backdrop" onClick={onClose} onContextMenu={(e) => { e.preventDefault(); onClose(); }} />
      <div className="ctx-menu" ref={ref} style={{ left: pos.x, top: pos.y }}>
        {menu.items.map((it, i) =>
          it.sep ? <div key={i} className="ctx-sep" /> : (
            <button key={i} className="ctx-item" onClick={() => { onClose(); it.onClick && it.onClick(); }}>
              <span className="ci-ico"><Icon name={it.icon} size={16} /></span>
              <span>{it.label}</span>
            </button>
          )
        )}
      </div>
    </React.Fragment>
  );
}

// =================================================================
// Vinyl sleeve hero
// =================================================================
function VinylSleeve({ album, playing }) {
  if (!album) return null;
  return (
    <div className="vinyl-stage">
      <div className={"vinyl-wrap" + (playing ? " playing" : "")}>
        <div className="vinyl-underglow" />
        <div className={"vinyl-disc" + (playing ? " spin" : "")} />
        <div className="vinyl-sleeve">
          <div className="art-fill"><CoverArt album={album} /></div>
          <div className="stripes" />
          <div className="inset-border" />
          <div className="sl-top">{album.side || "SIDE A"} · {album.year}</div>
          <div className="sl-bottom">
            <div className="sl-album">{album.name}</div>
            <div className="sl-artist">{album.albumArtist}</div>
          </div>
        </div>
      </div>
    </div>
  );
}

// =================================================================
// Slider
// =================================================================
function Slider({ value, onChange, dim, onCommit }) {
  return (
    <input
      type="range" className={"slider" + (dim ? " dim" : "")}
      min="0" max="1" step="0.001" value={value}
      onChange={(e) => onChange(parseFloat(e.target.value))}
      onMouseUp={() => onCommit && onCommit()}
    />
  );
}

// =================================================================
// Album card (grid)
// =================================================================
function AlbumCard({ album, onOpen, actions }) {
  const menu = useMenu();
  return (
    <div
      className="album-card"
      onClick={() => onOpen(album)}
      onContextMenu={(e) => { e.preventDefault(); menu.open(e, actions.albumMenu(album)); }}
    >
      <div className="cover-wrap">
        <CoverArt album={album} showLabel />
        <button
          className="card-play"
          onClick={(e) => { e.stopPropagation(); actions.playAlbum(album); }}
          title="Play album"
        >
          <Icon name="play" size={18} />
        </button>
      </div>
      <div>
        <div className="a-name">{album.name}</div>
        <div className="a-artist">{album.albumArtist} · {album.year}</div>
      </div>
    </div>
  );
}

// =================================================================
// Track row (album detail)
// =================================================================
function TrackRow({ track, index, isActive, onPlay, actions, showArtist }) {
  const menu = useMenu();
  const durSec = Math.floor(track.durationMs / 1000);
  return (
    <div
      className={"track-row" + (isActive ? " active" : "")}
      onClick={() => onPlay()}
      onContextMenu={(e) => { e.preventDefault(); menu.open(e, actions.trackMenu(track)); }}
    >
      <div className="tnum">
        {isActive
          ? <VuMeter playing={true} />
          : <React.Fragment>
              <span className="num">{String(index).padStart(2, "0")}</span>
              <span className="play-hint"><Icon name="play" size={13} /></span>
            </React.Fragment>}
      </div>
      <div className="t-meta">
        <div className="t-name">{track.name}</div>
        {showArtist && track.artist && (
          <div className="t-sub">{track.artist}</div>
        )}
      </div>
      <div className="t-right">
        {track.numberPlays > 0 && <Icon name="headphones" size={14} />}
        <span className="t-dur">{fmtTime(durSec)}</span>
        <button className="iconbtn" style={{ width: 30, height: 30 }}
          onClick={(e) => { e.stopPropagation(); menu.open(e, actions.trackMenu(track)); }}>
          <Icon name="ellipsis" size={16} />
        </button>
      </div>
    </div>
  );
}

// =================================================================
// Sidebar
// =================================================================
const NAV = [
  { id: "now", label: "Now Playing", icon: "disc" },
  { id: "library", label: "Library", icon: "library" },
  { id: "zones", label: "Zones", icon: "zones" },
  { id: "settings", label: "Settings", icon: "gear" },
];

function Sidebar({ route, onNavigate, np, player, activeZone, rail }) {
  const progress = np.durationMs > 0 ? (player.positionMs / np.durationMs) : 0;
  return (
    <aside className="sidebar">
      <div className="sb-brand">
        <div className="sb-mark" />
        {!rail && (
          <div className="sb-wordmark">
            <span className="lead">JRIVER</span>
            <span className="sub">REMOTE</span>
          </div>
        )}
      </div>

      <nav className="sb-nav">
        {NAV.map((n) => (
          <button
            key={n.id}
            className={"nav-item" + (route === n.id ? " active" : "")}
            onClick={() => onNavigate(n.id)}
            title={n.label}
          >
            <span className="ico"><Icon name={n.icon} size={20} /></span>
            {!rail && <span className="lbl">{n.label}</span>}
          </button>
        ))}
      </nav>

      <div className="sb-spacer" />

      {/* Docked now-playing cell */}
      <div className="sb-now" onClick={() => onNavigate("now")} title="Open Now Playing">
        <div className="progress" style={{ width: `${progress * 100}%` }} />
        <div className="sb-now-head">
          <div className="art"><CoverArt album={np.album} /></div>
          {!rail && (
            <div className="meta">
              <div className="ttl">{np.track ? np.track.name : "Idle"}</div>
              <div className="art-name">{np.track ? np.track.artist : "—"}</div>
            </div>
          )}
        </div>
        {!rail && (
          <div className="sb-now-ctl">
            <VuMeter playing={player.isPlaying} />
            <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
              <button className="iconbtn" style={{ width: 30, height: 30 }} onClick={(e) => { e.stopPropagation(); player.prev(); }}><Icon name="prev" size={16} /></button>
              <button className="iconbtn" style={{ width: 32, height: 32, color: "var(--accent)" }} onClick={(e) => { e.stopPropagation(); player.toggle(); }}><Icon name={player.isPlaying ? "pause" : "play"} size={18} /></button>
              <button className="iconbtn" style={{ width: 30, height: 30 }} onClick={(e) => { e.stopPropagation(); player.next(); }}><Icon name="next" size={16} /></button>
            </div>
          </div>
        )}
      </div>

      {/* Active zone */}
      <div className="sb-zone" onClick={() => onNavigate("zones")} title="Active zone">
        <span className="dot" />
        {!rail && (
          <div className="z-meta">
            <div className="z-label">Active Zone</div>
            <div className="z-name">{activeZone ? activeZone.name : "No Zone"}</div>
          </div>
        )}
      </div>
    </aside>
  );
}

Object.assign(window, {
  MenuCtx, useMenu, ContextMenuHost, VinylSleeve, Slider,
  AlbumCard, TrackRow, Sidebar, NAV, fmtTime, albumDuration,
});
