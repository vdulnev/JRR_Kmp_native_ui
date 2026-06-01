/* Screens: Now Playing, Library (Artists/Random/Favorites), Album Detail,
   Zones, Settings. Exports to window. */

const { useState: useStateS, useMemo: useMemoS, useRef: useRefS } = React;

// =================================================================
// NOW PLAYING
// =================================================================
function NowPlayingScreen({ np, player, queue, actions, tweaks }) {
  const menu = useMenu();
  const [scrub, setScrub] = useStateS(null);
  const dur = np.durationMs;
  const progress = scrub != null ? scrub : (dur > 0 ? player.positionMs / dur : 0);
  const curSec = (scrub != null ? scrub * dur : player.positionMs) / 1000;
  const focus = tweaks.npLayout === "focus";

  return (
    <div className="screen fade-in">
      <div className={"np" + (focus ? " focus" : "")}>
        <div className="np-hero">
          <div className="np-hero-bg"><CoverArt album={np.album} /></div>
          <div className="np-hero-inner">
            <VinylSleeve album={np.album} playing={player.isPlaying} />

            <div className="np-track-meta">
              <div className="np-title">{np.track ? np.track.name : "Idle"}</div>
              <div className="np-sub">{np.track ? `${np.track.artist} — ${np.album.name}` : "Nothing playing"}</div>
              {np.album && <div className="format-badge">FLAC · {np.album.sampleRate}KHZ / {np.album.bitDepth}-BIT</div>}
            </div>

            <div className="scrubber">
              <Slider value={progress}
                onChange={(v) => setScrub(v)}
                onCommit={() => { if (scrub != null) { player.seek(scrub * dur); setScrub(null); } }} />
              <div className="np-times">
                <span>{fmtTime(curSec)}</span>
                <span>-{fmtTime((dur / 1000) - curSec)}</span>
              </div>
            </div>

            <div className="transport">
              <button className={"tp-btn sec" + (player.shuffle ? " on" : "")} onClick={player.toggleShuffle} title="Shuffle"><Icon name="shuffle" size={20} /></button>
              <button className="tp-btn" onClick={player.prev} title="Previous"><Icon name="prev" size={26} /></button>
              <button className="tp-play" onClick={player.toggle} title="Play / Pause (Space)">
                <Icon name={player.isPlaying ? "pause" : "play"} size={26} />
              </button>
              <button className="tp-btn" onClick={player.next} title="Next"><Icon name="next" size={26} /></button>
              <button className={"tp-btn sec" + (player.repeat !== "off" ? " on" : "")} onClick={player.toggleRepeat} title="Repeat">
                <Icon name={player.repeat === "track" ? "repeat1" : "repeat"} size={20} />
              </button>
            </div>

            <div className="volume-row">
              <Icon name={player.volume === 0 ? "speaker-mute" : "speaker"} size={16} />
              <Slider value={player.volume} onChange={player.setVolume} dim />
              <span className="vval">{Math.round(player.volume * 100)}</span>
            </div>
          </div>
        </div>

        {!focus && (
          <QueueRail np={np} player={player} queue={queue} actions={actions} />
        )}
      </div>
    </div>
  );
}

// ---- Queue rail (Up Next) ----
function QueueRail({ np, player, queue, actions }) {
  const menu = useMenu();
  const [dragIdx, setDragIdx] = useStateS(null);
  const [overIdx, setOverIdx] = useStateS(null);

  return (
    <div className="queue-rail">
      <div className="qr-head">
        <div>
          <div className="t-section-label">Play Queue</div>
          <div className="t-mono" style={{ marginTop: 4 }}>{queue.length} TRACKS · {np.activeZoneName}</div>
        </div>
        <div className="qr-actions">
          <button className="qr-btn danger" onClick={actions.clearQueue}>CLEAR</button>
        </div>
      </div>
      <div className="queue-scroll scroll">
        {queue.map((t, i) => {
          const active = i === player.currentIndex;
          return (
            <div
              key={t.fileKey}
              className={"queue-item" + (active ? " active" : "") + (dragIdx === i ? " dragging" : "") + (overIdx === i && dragIdx !== null && dragIdx !== i ? " drop-before" : "")}
              draggable
              onDragStart={() => setDragIdx(i)}
              onDragOver={(e) => { e.preventDefault(); setOverIdx(i); }}
              onDragEnd={() => { if (dragIdx != null && overIdx != null) actions.moveQueue(dragIdx, overIdx); setDragIdx(null); setOverIdx(null); }}
              onClick={() => actions.playQueueIndex(i)}
              onContextMenu={(e) => { e.preventDefault(); menu.open(e, [
                { label: "Play", icon: "play", onClick: () => actions.playQueueIndex(i) },
                { label: "Remove from Queue", icon: "x", onClick: () => actions.removeQueue(i) },
                { sep: true },
                { label: "Info", icon: "info", onClick: () => actions.toast("Track info") },
              ]); }}
            >
              <div className="q-idx">{active ? <VuMeter playing={player.isPlaying} /> : String(i + 1).padStart(2, "0")}</div>
              <div className="q-meta">
                <div className="q-name">{t.name}</div>
                <div className="q-artist">{t.artist}</div>
              </div>
              <span className="q-dur">{fmtTime(t.durationMs / 1000)}</span>
              <span className="q-grip"><Icon name="grip" size={16} /></span>
            </div>
          );
        })}
        {queue.length === 0 && <div style={{ padding: "40px 8px", textAlign: "center" }} className="t-mono">QUEUE IS EMPTY</div>}
      </div>
    </div>
  );
}

// =================================================================
// LIBRARY
// =================================================================
const LIB_TABS = [
  { id: "artists", label: "Artists" },
  { id: "random", label: "Random" },
  { id: "favorites", label: "Favorites" },
  { id: "downloads", label: "Downloads" },
];

function LibraryScreen({ lib, actions, favorites, tweaks }) {
  return (
    <div className="screen fade-in">
      <div className="screen-head">
        <div className="h-left">
          <span className="t-section-label">Library</span>
          <span className="t-screen-title">Browse</span>
        </div>
      </div>
      <div className="tabs">
        {LIB_TABS.map((t) => (
          <button key={t.id} className={"tab" + (lib.tab === t.id ? " active" : "")} onClick={() => actions.setLibTab(t.id)}>{t.label}</button>
        ))}
      </div>
      {lib.tab === "artists" && <ArtistsTab lib={lib} actions={actions} />}
      {lib.tab === "random" && <GridTab albums={lib.randomAlbums} actions={actions} meta={`SHUFFLED · ${lib.randomAlbums.length} ALBUMS`} onRefresh={actions.reshuffle} />}
      {lib.tab === "favorites" && <FavoritesTab favorites={favorites} actions={actions} />}
      {lib.tab === "downloads" && <GridTab albums={lib.downloads} actions={actions} meta={`${lib.downloads.length} ALBUMS AVAILABLE OFFLINE`} />}
    </div>
  );
}

function ArtistsTab({ lib, actions }) {
  const [filter, setFilter] = useStateS("");
  const artists = window.DATA.artists.filter((a) => a.name.toLowerCase().includes(filter.toLowerCase()));
  const selected = lib.selectedArtist;
  const albums = selected ? window.DATA.byArtist[selected] || [] : [];

  return (
    <div className="split wide">
      <div className="master">
        <div className="master-head">
          <div className="filter-field">
            <Icon name="search" size={16} />
            <input placeholder="Filter artists" value={filter} onChange={(e) => setFilter(e.target.value)} />
            {filter && <button className="iconbtn" style={{ width: 24, height: 24 }} onClick={() => setFilter("")}><Icon name="x" size={14} /></button>}
          </div>
        </div>
        <div className="list-scroll scroll">
          {artists.map((a) => (
            <button key={a.name} className={"artist-row" + (selected === a.name ? " sel" : "")} onClick={() => actions.selectArtist(a.name)}>
              <span className="avatar">{a.name.replace(/^The /, "").charAt(0)}</span>
              <span className="a-label">{a.name}</span>
              <span className="a-count">{a.albumCount}</span>
            </button>
          ))}
        </div>
      </div>
      <div className="detail-pane">
        {selected ? (
          <React.Fragment>
            <div className="screen-head" style={{ paddingTop: 22 }}>
              <div className="h-left">
                <span className="t-section-label">{albums.length} {albums.length === 1 ? "Album" : "Albums"}</span>
                <span className="t-sub-title">{selected}</span>
              </div>
              <div className="h-actions">
                <button className="btn btn-ghost" style={{ flex: "none", padding: "0 16px", height: 38 }} onClick={() => actions.playArtist(selected)}>
                  <Icon name="play" size={13} /> PLAY ALL
                </button>
              </div>
            </div>
            <div className="grid-scroll scroll">
              <div className="album-grid">
                {albums.map((al) => <AlbumCard key={al.id} album={al} onOpen={actions.openAlbum} actions={actions} />)}
              </div>
            </div>
          </React.Fragment>
        ) : (
          <div className="empty-state">
            <span className="es-ico"><Icon name="library" size={42} /></span>
            <div className="es-title">Select an artist</div>
            <div className="es-sub">Pick a name on the left to browse their albums.</div>
          </div>
        )}
      </div>
    </div>
  );
}

function GridTab({ albums, actions, meta, onRefresh }) {
  return (
    <React.Fragment>
      <div className="screen-head" style={{ paddingTop: 16, paddingBottom: 6 }}>
        <span className="t-mono">{meta}</span>
        {onRefresh && (
          <button className="btn btn-ghost" style={{ flex: "none", padding: "0 14px", height: 34 }} onClick={onRefresh}>
            <Icon name="refresh" size={14} /> REFRESH
          </button>
        )}
      </div>
      <div className="grid-scroll scroll">
        <div className="album-grid">
          {albums.map((al) => <AlbumCard key={al.id} album={al} onOpen={actions.openAlbum} actions={actions} />)}
        </div>
      </div>
    </React.Fragment>
  );
}

function FavoritesTab({ favorites, actions }) {
  const favAlbums = window.DATA.albums.filter((a) => favorites.has(a.id));
  if (favAlbums.length === 0) {
    return (
      <div className="empty-state">
        <span className="es-ico"><Icon name="star-fill" size={42} /></span>
        <div className="es-title">Your Favorites</div>
        <div className="es-sub">Star an album and it’ll be pinned here.</div>
      </div>
    );
  }
  return <GridTab albums={favAlbums} actions={actions} meta={`${favAlbums.length} PINNED ALBUMS`} />;
}

// =================================================================
// ALBUM DETAIL
// =================================================================
function AlbumDetailScreen({ album, player, actions, favorites }) {
  const menu = useMenu();
  const isFav = favorites.has(album.id);
  const discs = useMemoS(() => {
    const groups = {};
    album.tracks.forEach((t) => { (groups[t.discNumber] = groups[t.discNumber] || []).push(t); });
    return Object.keys(groups).map(Number).sort((a, b) => a - b).map((d) => ({ disc: d, tracks: groups[d] }));
  }, [album]);
  const multiDisc = discs.length > 1;
  const activeKey = player.currentTrack ? player.currentTrack.fileKey : null;

  return (
    <div className="screen fade-in">
      <div className="screen-head" style={{ paddingBottom: 0 }}>
        <button className="btn btn-ghost" style={{ flex: "none", padding: "0 14px", height: 36 }} onClick={actions.back}>
          <Icon name="chevron-left" size={16} /> BACK
        </button>
        <span className="t-section-label">Album</span>
        <div className="h-actions">
          <button className={"iconbtn" + (isFav ? " on" : "")} onClick={() => actions.toggleFavorite(album.id)} title="Favorite">
            <Icon name={isFav ? "star-fill" : "star"} size={20} />
          </button>
          <button className="iconbtn" onClick={(e) => menu.open(e, actions.albumMenu(album))}><Icon name="ellipsis" size={18} /></button>
        </div>
      </div>

      <div className="detail">
        <div className="detail-art-col scroll">
          <div className="detail-art"><CoverArt album={album} /></div>
          <div className="detail-meta">
            <div className="d-name">{album.name}</div>
            <div className="d-artist">{album.albumArtist}</div>
          </div>
          <div className="detail-stats">
            <span className="stat">{album.year}</span>
            <span className="stat">{album.tracks.length} Tracks</span>
            <span className="stat">{albumDuration(album)}</span>
          </div>
          <div className="detail-stats">
            <span className="stat" style={{ color: "var(--accent)" }}>FLAC {album.sampleRate}K / {album.bitDepth}-BIT</span>
          </div>
          <div className="detail-actions">
            <button className="btn btn-primary" onClick={() => actions.playAlbum(album)}><Icon name="play" size={13} /> PLAY</button>
            <button className="btn btn-ghost" onClick={() => actions.shuffleAlbum(album)}><Icon name="shuffle" size={14} /> SHUFFLE</button>
          </div>
        </div>

        <div className="tracklist scroll">
          {discs.map((d) => (
            <div key={d.disc}>
              <div className="disc-head">
                <span className="t-section-label">{multiDisc ? `Disc ${d.disc}` : (album.side || "SIDE A")}</span>
              </div>
              {d.tracks.map((t, i) => (
                <React.Fragment key={t.fileKey}>
                  <TrackRow
                    track={t}
                    index={t.trackNumber}
                    isActive={activeKey === t.fileKey}
                    showArtist={t.artist !== album.albumArtist}
                    onPlay={() => actions.playTrackInAlbum(album, t)}
                    actions={actions}
                  />
                  {i < d.tracks.length - 1 && <div className="row-divider" />}
                </React.Fragment>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// =================================================================
// ZONES
// =================================================================
function ZonesScreen({ zones, actions }) {
  return (
    <div className="screen fade-in">
      <div className="screen-head">
        <div className="h-left">
          <span className="t-section-label">Output</span>
          <span className="t-screen-title">Zones</span>
        </div>
        <button className="iconbtn" onClick={actions.refreshZones} title="Refresh"><Icon name="refresh" size={18} /></button>
      </div>
      <div className="zones-scroll scroll">
        <div className="zone-section-label t-section-heading">Server Outputs</div>
        <div className="zone-grid">
          {zones.server.map((z) => <ZoneCard key={z.id} zone={z} actions={actions} />)}
        </div>
        <div className="zone-section-label t-section-heading" style={{ marginTop: 28 }}>On This Device</div>
        <div className="zone-grid">
          {zones.device.map((z) => <ZoneCard key={z.id} zone={z} actions={actions} />)}
        </div>
      </div>
    </div>
  );
}

function ZoneCard({ zone, actions }) {
  const icon = zone.type.includes("Local") ? "monitor" : zone.type.includes("Car") ? "car" : "speaker";
  return (
    <div className={"zone-card" + (zone.active ? " active" : "")} onClick={() => actions.selectZone(zone.id)}>
      <div className="zone-card-top">
        <div className="z-icon"><Icon name={icon} size={20} /></div>
        <div className="z-info">
          <div className="z-name">{zone.name}</div>
          <div className="z-type">{zone.type}</div>
        </div>
        {zone.active && <span className="z-active-tag">ACTIVE</span>}
      </div>
      {zone.active && (
        <div className="z-vol" onClick={(e) => e.stopPropagation()}>
          <Icon name="speaker" size={16} />
          <Slider value={zone.vol} onChange={(v) => actions.setZoneVol(zone.id, v)} />
          <span className="vval" style={{ color: "var(--text3)" }}>{Math.round(zone.vol * 100)}</span>
        </div>
      )}
    </div>
  );
}

// =================================================================
// SETTINGS
// =================================================================
function SettingsScreen({ settings, actions }) {
  const QUALITY = [
    { id: "lossless", label: "LOSSLESS" },
    { id: "high", label: "HIGH 320" },
    { id: "low", label: "LOW 128" },
  ];
  const SEV = ["V", "D", "I", "W", "E"];
  return (
    <div className="screen fade-in">
      <div className="screen-head">
        <div className="h-left">
          <span className="t-section-label">App</span>
          <span className="t-screen-title">Settings</span>
        </div>
      </div>
      <div className="settings-scroll scroll">
        <div className="settings-col">
          <div className="set-card">
            <div className="set-label">Current Connection</div>
            <div className="set-row"><span className="sr-key">Host</span><span className="sr-val">media-server.local</span></div>
            <div className="set-row"><span className="sr-key">Port</span><span className="sr-val">52199 (HTTP)</span></div>
            <div className="set-row"><span className="sr-key">Library</span><span className="sr-val">{window.DATA.albums.length} albums · 6,214 tracks</span></div>
            <div style={{ marginTop: 14 }}>
              <button className="set-btn danger" onClick={() => actions.toast("Disconnected (demo)")}>DISCONNECT / CHANGE SERVER</button>
            </div>
          </div>

          <div className="set-card">
            <div className="set-label">Audio Quality — Streaming &amp; Downloads</div>
            <p className="set-desc">Server transcodes on the fly. Lossless preserves fidelity; lossy saves bandwidth.</p>
            <div className="seg">
              {QUALITY.map((q) => (
                <button key={q.id} className={settings.quality === q.id ? "sel" : ""} onClick={() => actions.setQuality(q.id)}>{q.label}</button>
              ))}
            </div>
          </div>

          <div className="set-card">
            <div className="set-label">Storage &amp; Downloads</div>
            <div className="set-row"><span className="sr-key">Downloaded tracks</span><span className="sr-val">128 cached</span></div>
            <div className="set-row"><span className="sr-key">Occupied</span><span className="sr-val">3.7 GB</span></div>
            <div style={{ marginTop: 14 }}>
              <button className="set-btn danger" onClick={() => actions.toast("Downloads cleared (demo)")}>CLEAR DOWNLOADS</button>
            </div>
          </div>

          <div className="set-card">
            <div className="set-label">Logging</div>
            <p className="set-desc">Recent activity from the in-memory ring buffer.</p>
            <div style={{ display: "flex", gap: 10, marginTop: 12, marginBottom: 16 }}>
              <button className="set-btn accent" onClick={() => actions.toast("Log exported (demo)")}>SHARE LOG</button>
            </div>
            <div className="set-label" style={{ marginBottom: 8 }}>Min Severity</div>
            <div className="seg">
              {SEV.map((s) => (
                <button key={s} className={settings.severity === s ? "sel" : ""} onClick={() => actions.setSeverity(s)}>{s}</button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, {
  NowPlayingScreen, LibraryScreen, AlbumDetailScreen, ZonesScreen, SettingsScreen, LIB_TABS,
});
