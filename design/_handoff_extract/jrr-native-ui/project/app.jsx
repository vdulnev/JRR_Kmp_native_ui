/* App root: state, player engine, routing, keyboard shortcuts,
   context-menu host, toast, tweaks. */

const { useState: uS, useEffect: uE, useRef: uR, useCallback: uC, useMemo: uM } = React;

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "density": "desktop",
  "sidebar": "expanded",
  "npLayout": "split",
  "accent": "#C8922A"
}/*EDITMODE-END*/;

const ACCENTS = {
  "#C8922A": "200, 146, 42",   // gold (default)
  "#5B86C8": "91, 134, 200",   // steel blue
  "#5BCE8A": "91, 206, 138",   // green
  "#C85B7A": "200, 91, 122",   // rose
};

function shuffleArr(a) {
  const b = a.slice();
  for (let i = b.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [b[i], b[j]] = [b[j], b[i]]; }
  return b;
}

function App() {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);

  // ---- routing ----
  const [route, setRoute] = uS("now");
  const [albumView, setAlbumView] = uS(null);     // album object when viewing detail
  const [backTo, setBackTo] = uS("library");

  // ---- library ----
  const [libTab, setLibTab] = uS("artists");
  const [selectedArtist, setSelectedArtist] = uS("Pink Floyd");
  const [randomAlbums, setRandomAlbums] = uS(() => shuffleArr(window.DATA.albums));
  const downloads = uM(() => window.DATA.albums.slice(0, 6), []);

  // ---- favorites ----
  const [favorites, setFavorites] = uS(() => new Set(["pf-wywh", "md-kind-of-blue", "db-time-out"]));

  // ---- zones ----
  const [zones, setZones] = uS(() => JSON.parse(JSON.stringify(window.DATA.zones)));
  const activeZone = uM(() => [...zones.server, ...zones.device].find((z) => z.active), [zones]);

  // ---- settings ----
  const [settings, setSettings] = uS({ quality: "lossless", severity: "I" });

  // ---- player ----
  const [queue, setQueue] = uS(() => window.DATA.queue);
  const [currentIndex, setCurrentIndex] = uS(0);
  const [isPlaying, setIsPlaying] = uS(false);
  const [positionMs, setPositionMs] = uS(0);
  const [volume, setVolume] = uS(0.62);
  const [shuffle, setShuffle] = uS(false);
  const [repeat, setRepeat] = uS("off"); // off | all | track

  // ---- menu + toast ----
  const [menu, setMenu] = uS(null);
  const [toastMsg, setToastMsg] = uS(null);
  const toastTimer = uR(null);

  const currentTrack = queue[currentIndex] || null;
  const currentAlbum = uM(() => {
    if (!currentTrack) return window.DATA.dsotm;
    return window.DATA.albums.find((a) => a.id === currentTrack.albumId) || window.DATA.dsotm;
  }, [currentTrack]);
  const durationMs = currentTrack ? currentTrack.durationMs : 0;

  // ---- apply tweaks to CSS vars ----
  const appRef = uR(null);
  uE(() => {
    const el = appRef.current; if (!el) return;
    if (t.density === "ipad") {
      el.style.setProperty("--gutter", "26px");
      el.style.setProperty("--grid-min", "150px");
    } else {
      el.style.setProperty("--gutter", "40px");
      el.style.setProperty("--grid-min", "188px");
    }
    el.style.setProperty("--accent", t.accent);
    el.style.setProperty("--accent-rgb", ACCENTS[t.accent] || "200, 146, 42");
    const rgb = ACCENTS[t.accent] || "200, 146, 42";
    el.style.setProperty("--accent-dim", `rgba(${rgb}, 0.13)`);
    el.style.setProperty("--accent-soft", `rgba(${rgb}, 0.32)`);
  }, [t.density, t.accent]);

  // ---- toast ----
  const toast = uC((msg) => {
    setToastMsg(msg);
    clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToastMsg(null), 1900);
  }, []);

  // ---- player engine ----
  uE(() => {
    if (!isPlaying) return;
    const iv = setInterval(() => {
      setPositionMs((p) => {
        if (p + 250 >= durationMs) {
          // advance
          handleNext(true);
          return 0;
        }
        return p + 250;
      });
    }, 250);
    return () => clearInterval(iv);
  }, [isPlaying, durationMs, currentIndex, queue, repeat]);

  const playFrom = uC((newQueue, idx) => {
    setQueue(newQueue);
    setCurrentIndex(idx);
    setPositionMs(0);
    setIsPlaying(true);
  }, []);

  const toggle = uC(() => setIsPlaying((v) => !v), []);

  const handleNext = uC((auto) => {
    setCurrentIndex((i) => {
      if (repeat === "track" && auto) { setPositionMs(0); return i; }
      let n = i + 1;
      if (n >= queue.length) { if (repeat === "all" || !auto) n = 0; else { setIsPlaying(false); return i; } }
      setPositionMs(0);
      return n;
    });
  }, [queue.length, repeat]);

  const handlePrev = uC(() => {
    setPositionMs((p) => {
      if (p > 3000) return 0; // restart current
      setCurrentIndex((i) => (i - 1 + queue.length) % queue.length);
      return 0;
    });
  }, [queue.length]);

  const seek = uC((ms) => setPositionMs(Math.max(0, Math.min(ms, durationMs))), [durationMs]);

  // ---- queue ops ----
  const albumToQueueItems = (album, tracks) => (tracks || album.tracks).map((t) => ({ ...t, album: album.name, albumArtist: album.albumArtist, albumId: album.id }));

  const playAlbum = uC((album) => {
    playFrom(albumToQueueItems(album), 0);
    toast(`Playing ${album.name}`);
  }, [playFrom, toast]);

  const shuffleAlbum = uC((album) => {
    playFrom(shuffleArr(albumToQueueItems(album)), 0);
    setShuffle(true);
    toast(`Shuffling ${album.name}`);
  }, [playFrom, toast]);

  const playArtist = uC((artistName) => {
    const all = (window.DATA.byArtist[artistName] || []).flatMap((al) => albumToQueueItems(al));
    if (all.length) { playFrom(all, 0); toast(`Playing ${artistName}`); }
  }, [playFrom, toast]);

  const playTrackInAlbum = uC((album, track) => {
    const items = albumToQueueItems(album);
    const idx = items.findIndex((x) => x.fileKey === track.fileKey);
    playFrom(items, idx < 0 ? 0 : idx);
  }, [playFrom]);

  const addToQueue = uC((album, track) => {
    const items = track ? albumToQueueItems(album, [track]) : albumToQueueItems(album);
    setQueue((q) => [...q, ...items]);
    toast(track ? `Added “${track.name}” to queue` : `Added ${items.length} tracks to queue`);
  }, [toast]);

  const playNext = uC((album, track) => {
    const items = track ? albumToQueueItems(album, [track]) : albumToQueueItems(album);
    setQueue((q) => { const c = q.slice(); c.splice(currentIndex + 1, 0, ...items); return c; });
    toast("Will play next");
  }, [currentIndex, toast]);

  const moveQueue = uC((from, to) => {
    setQueue((q) => { const c = q.slice(); const [it] = c.splice(from, 1); c.splice(to, 0, it); return c; });
    setCurrentIndex((ci) => {
      if (ci === from) return to;
      if (from < ci && to >= ci) return ci - 1;
      if (from > ci && to <= ci) return ci + 1;
      return ci;
    });
  }, []);

  const removeQueue = uC((idx) => {
    setQueue((q) => q.filter((_, i) => i !== idx));
    setCurrentIndex((ci) => idx < ci ? ci - 1 : ci);
  }, []);

  const clearQueue = uC(() => { setQueue([]); setIsPlaying(false); toast("Queue cleared"); }, [toast]);

  const playQueueIndex = uC((i) => { setCurrentIndex(i); setPositionMs(0); setIsPlaying(true); }, []);

  // ---- nav ----
  const openAlbum = uC((album) => { setBackTo("library"); setAlbumView(album); setRoute("album"); }, []);
  const navigate = uC((r) => { setRoute(r); }, []);
  const back = uC(() => { setRoute(backTo); }, [backTo]);

  const toggleFavorite = uC((id) => {
    setFavorites((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });
  }, []);

  // ---- zones ----
  const selectZone = uC((id) => {
    setZones((z) => ({
      server: z.server.map((x) => ({ ...x, active: x.id === id })),
      device: z.device.map((x) => ({ ...x, active: x.id === id })),
    }));
    const all = [...zones.server, ...zones.device];
    const z = all.find((x) => x.id === id);
    if (z) toast(`Switched to ${z.name}`);
  }, [zones, toast]);

  const setZoneVol = uC((id, v) => {
    setZones((z) => ({
      server: z.server.map((x) => x.id === id ? { ...x, vol: v } : x),
      device: z.device.map((x) => x.id === id ? { ...x, vol: v } : x),
    }));
  }, []);

  // ---- context menu builders ----
  const albumMenu = uC((album) => ([
    { label: "Play", icon: "play", onClick: () => playAlbum(album) },
    { label: "Play Next", icon: "play-next", onClick: () => playNext(album) },
    { label: "Add to Queue", icon: "plus", onClick: () => addToQueue(album) },
    { sep: true },
    { label: favorites.has(album.id) ? "Remove Favorite" : "Add to Favorites", icon: favorites.has(album.id) ? "star-fill" : "star", onClick: () => toggleFavorite(album.id) },
    { label: "Download Album", icon: "download", onClick: () => toast(`Downloading ${album.name}`) },
    { sep: true },
    { label: "Info", icon: "info", onClick: () => toast(`${album.name} · ${album.year}`) },
  ]), [favorites, playAlbum, playNext, addToQueue, toggleFavorite, toast]);

  const trackMenu = uC((track) => {
    const album = window.DATA.albums.find((a) => a.id === track.albumId) || albumView || currentAlbum;
    return [
      { label: "Play", icon: "play", onClick: () => playTrackInAlbum(album, track) },
      { label: "Play Next", icon: "play-next", onClick: () => playNext(album, track) },
      { label: "Add to Queue", icon: "plus", onClick: () => addToQueue(album, track) },
      { sep: true },
      { label: "Download", icon: "download", onClick: () => toast(`Downloading “${track.name}”`) },
      { label: "Info", icon: "info", onClick: () => toast(track.name) },
    ];
  }, [albumView, currentAlbum, playTrackInAlbum, playNext, addToQueue, toast]);

  const openMenu = uC((e, items) => setMenu({ x: e.clientX, y: e.clientY, items }), []);

  // ---- keyboard ----
  uE(() => {
    const onKey = (e) => {
      const tag = (e.target.tagName || "").toLowerCase();
      if (tag === "input" || tag === "textarea") return;
      switch (e.key) {
        case " ": e.preventDefault(); toggle(); break;
        case "ArrowRight": if (!e.metaKey) { e.preventDefault(); handleNext(false); } break;
        case "ArrowLeft": if (!e.metaKey) { e.preventDefault(); handlePrev(); } break;
        case "ArrowUp": e.preventDefault(); setVolume((v) => Math.min(1, v + 0.05)); break;
        case "ArrowDown": e.preventDefault(); setVolume((v) => Math.max(0, v - 0.05)); break;
        case "q": case "Q": setRoute("now"); break;
        case "l": case "L": setRoute("library"); break;
        default: break;
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [toggle, handleNext, handlePrev]);

  // ---- assemble ----
  const player = {
    isPlaying, positionMs, volume, shuffle, repeat, currentIndex, currentTrack,
    toggle, next: () => handleNext(false), prev: handlePrev, seek,
    setVolume, toggleShuffle: () => setShuffle((v) => !v),
    toggleRepeat: () => setRepeat((r) => r === "off" ? "all" : r === "all" ? "track" : "off"),
  };
  const np = { track: currentTrack, album: currentAlbum, durationMs, activeZoneName: activeZone ? activeZone.name.toUpperCase() : "NO ZONE" };

  const actions = {
    openAlbum, back, playAlbum, shuffleAlbum, playArtist, playTrackInAlbum,
    addToQueue, playNext, moveQueue, removeQueue, clearQueue, playQueueIndex,
    toggleFavorite, selectArtist: setSelectedArtist, setLibTab,
    reshuffle: () => setRandomAlbums(shuffleArr(window.DATA.albums)),
    selectZone, setZoneVol, refreshZones: () => toast("Zones refreshed"),
    setQuality: (q) => setSettings((s) => ({ ...s, quality: q })),
    setSeverity: (s) => setSettings((p) => ({ ...p, severity: s })),
    albumMenu, trackMenu, toast,
  };

  const lib = { tab: libTab, selectedArtist, randomAlbums, downloads };
  const rail = t.sidebar === "rail";

  return (
    <MenuCtx.Provider value={{ open: openMenu }}>
      <div className={"app" + (rail ? " rail" : "")} ref={appRef}>
        <Sidebar route={route === "album" ? "library" : route} onNavigate={navigate} np={np} player={player} activeZone={activeZone} rail={rail} />
        <div className="main">
          {route === "now" && <NowPlayingScreen np={np} player={player} queue={queue} actions={actions} tweaks={t} />}
          {route === "library" && <LibraryScreen lib={lib} actions={actions} favorites={favorites} tweaks={t} />}
          {route === "album" && albumView && <AlbumDetailScreen album={albumView} player={player} actions={actions} favorites={favorites} />}
          {route === "zones" && <ZonesScreen zones={zones} actions={actions} />}
          {route === "settings" && <SettingsScreen settings={settings} actions={actions} />}

          <div className="kbar">
            <div className="kh"><span className="kbd">Space</span> Play / Pause</div>
            <div className="kh"><span className="kbd">←</span><span className="kbd">→</span> Prev / Next</div>
            <div className="kh"><span className="kbd">↑</span><span className="kbd">↓</span> Volume</div>
            <div className="kh"><span className="kbd">Q</span> Now Playing</div>
            <div className="kh"><span className="kbd">L</span> Library</div>
            <div className="kh" style={{ marginLeft: "auto" }}>Right-click albums &amp; tracks for actions</div>
          </div>
        </div>

        <ContextMenuHost menu={menu} onClose={() => setMenu(null)} />
        {toastMsg && <div className="toast">{toastMsg}</div>}

        <TweaksPanel>
          <TweakSection label="Form factor" />
          <TweakRadio label="Density" value={t.density} options={["ipad", "desktop"]} onChange={(v) => setTweak("density", v)} />
          <TweakRadio label="Sidebar" value={t.sidebar} options={["expanded", "rail"]} onChange={(v) => setTweak("sidebar", v)} />
          <TweakSection label="Now Playing" />
          <TweakRadio label="Layout" value={t.npLayout} options={["split", "focus"]} onChange={(v) => setTweak("npLayout", v)} />
          <TweakSection label="Accent" />
          <TweakColor label="Color" value={t.accent} options={Object.keys(ACCENTS)} onChange={(v) => setTweak("accent", v)} />
        </TweaksPanel>
      </div>
    </MenuCtx.Provider>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
