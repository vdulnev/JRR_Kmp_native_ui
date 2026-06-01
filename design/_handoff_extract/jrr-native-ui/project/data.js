/* Mock library data for the JRiver Remote large-screen prototype.
   Curated to mirror the real collection (Pink Floyd, Sinatra, Brubeck,
   Moroder, FSOL, classical, etc.). Attached to window.DATA. */
(function () {
  // duration helper (m:ss -> ms)
  const ms = (m, s) => (m * 60 + s) * 1000;

  // Build a track list quickly
  function tracks(albumArtist, list) {
    return list.map((t, i) => ({
      fileKey: Math.random().toString(36).slice(2),
      trackNumber: i + 1,
      discNumber: t[2] || 1,
      name: t[0],
      artist: t[3] || albumArtist,
      durationMs: ms(t[1][0], t[1][1]),
      numberPlays: Math.random() > 0.6 ? Math.floor(Math.random() * 12) : 0,
    }));
  }

  const albums = [
    {
      id: "pf-dsotm", name: "The Dark Side of the Moon", albumArtist: "Pink Floyd",
      year: "1973", genre: "Progressive Rock", sampleRate: 192, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Pink Floyd", [
        ["Speak to Me", [1, 13]], ["Breathe (In the Air)", [2, 43]], ["On the Run", [3, 36]],
        ["Time", [6, 53]], ["The Great Gig in the Sky", [4, 36]], ["Money", [6, 23]],
        ["Us and Them", [7, 49]], ["Any Colour You Like", [3, 26]], ["Brain Damage", [3, 49]],
        ["Eclipse", [2, 3]],
      ]),
    },
    {
      id: "pf-wywh", name: "Wish You Were Here", albumArtist: "Pink Floyd",
      year: "1975", genre: "Progressive Rock", sampleRate: 96, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Pink Floyd", [
        ["Shine On You Crazy Diamond (Pts. I–V)", [13, 31]], ["Welcome to the Machine", [7, 31]],
        ["Have a Cigar", [5, 8]], ["Wish You Were Here", [5, 21]],
        ["Shine On You Crazy Diamond (Pts. VI–IX)", [12, 23]],
      ]),
    },
    {
      id: "pf-animals", name: "Animals", albumArtist: "Pink Floyd",
      year: "1977", genre: "Progressive Rock", sampleRate: 96, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Pink Floyd", [
        ["Pigs on the Wing 1", [1, 25]], ["Dogs", [17, 4]], ["Pigs (Three Different Ones)", [11, 25]],
        ["Sheep", [10, 25]], ["Pigs on the Wing 2", [1, 27]],
      ]),
    },
    {
      id: "dg-rattle", name: "Rattle That Lock", albumArtist: "David Gilmour",
      year: "2015", genre: "Rock", sampleRate: 96, bitDepth: 24, side: "SIDE A",
      tracks: tracks("David Gilmour", [
        ["5 A.M.", [3, 5]], ["Rattle That Lock", [5, 4]], ["Faces of Stone", [4, 27]],
        ["A Boat Lies Waiting", [4, 43]], ["Dancing Right in Front of Me", [4, 14]],
        ["In Any Tongue", [6, 44]], ["Beauty", [3, 17]],
      ]),
    },
    {
      id: "fs-sinatra-basie", name: "Sinatra–Basie: An Historic Musical First", albumArtist: "Frank Sinatra",
      year: "1962", genre: "Vocal Jazz", sampleRate: 96, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Frank Sinatra", [
        ["Pennies from Heaven", [2, 43]], ["Please Be Kind", [2, 28]], ["(Love Is) The Tender Trap", [2, 18]],
        ["Looking at the World Thru Rose Colored Glasses", [2, 11]], ["My Kind of Girl", [3, 8]],
        ["I Only Have Eyes for You", [3, 1]], ["Nice Work If You Can Get It", [2, 22]],
      ]),
    },
    {
      id: "fs-wee", name: "In the Wee Small Hours", albumArtist: "Frank Sinatra",
      year: "1955", genre: "Vocal Jazz", sampleRate: 44, bitDepth: 16, side: "SIDE A",
      tracks: tracks("Frank Sinatra", [
        ["In the Wee Small Hours of the Morning", [3, 0]], ["Mood Indigo", [3, 30]],
        ["Glad to Be Unhappy", [2, 35]], ["I Get Along Without You Very Well", [3, 42]],
        ["Deep in a Dream", [2, 49]], ["I See Your Face Before Me", [3, 24]],
        ["Can't We Be Friends?", [2, 48]], ["When Your Lover Has Gone", [3, 7]],
      ]),
    },
    {
      id: "db-time-out", name: "Time Out", albumArtist: "Dave Brubeck",
      year: "1959", genre: "Jazz", sampleRate: 192, bitDepth: 24, side: "SIDE A",
      tracks: tracks("The Dave Brubeck Quartet", [
        ["Blue Rondo à la Turk", [6, 44]], ["Strange Meadow Lark", [7, 22]], ["Take Five", [5, 24]],
        ["Three to Get Ready", [5, 24]], ["Kathy's Waltz", [4, 48]], ["Everybody's Jumpin'", [4, 23]],
        ["Pick Up Sticks", [4, 16]],
      ]),
    },
    {
      id: "md-kind-of-blue", name: "Kind of Blue", albumArtist: "Miles Davis",
      year: "1959", genre: "Jazz", sampleRate: 192, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Miles Davis", [
        ["So What", [9, 22]], ["Freddie Freeloader", [9, 46]], ["Blue in Green", [5, 37]],
        ["All Blues", [11, 33]], ["Flamenco Sketches", [9, 26]],
      ]),
    },
    {
      id: "be-waltz", name: "Waltz for Debby", albumArtist: "Bill Evans",
      year: "1961", genre: "Jazz", sampleRate: 96, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Bill Evans Trio", [
        ["My Foolish Heart", [4, 56]], ["Waltz for Debby (Take 2)", [6, 57]], ["Detour Ahead (Take 2)", [7, 33]],
        ["My Romance (Take 1)", [7, 13]], ["Some Other Time", [5, 1]], ["Milestones", [6, 32]],
      ]),
    },
    {
      id: "gm-from-here", name: "From Here to Eternity", albumArtist: "Giorgio Moroder",
      year: "1977", genre: "Electronic", sampleRate: 48, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Giorgio Moroder", [
        ["From Here to Eternity", [3, 56]], ["Faster Than the Speed of Love", [3, 28]],
        ["Lost Angeles", [4, 26]], ["Utopia – Me Giorgio", [5, 24]], ["From Here to Eternity (Reprise)", [1, 9]],
        ["Too Hot to Handle", [4, 14]], ["First Hand Experience in Second Hand Love", [4, 8]],
      ]),
    },
    {
      id: "fsol-lifeforms", name: "Lifeforms", albumArtist: "The Future Sound of London",
      year: "1994", genre: "Ambient", sampleRate: 44, bitDepth: 16, side: "SIDE A",
      tracks: tracks("The Future Sound of London", [
        ["Cascade (Part 1)", [5, 1]], ["Ill Flower", [4, 16]], ["Flak", [6, 23]],
        ["Bird Wings", [3, 35]], ["Dreamtime", [4, 22]], ["Domain", [5, 13]],
        ["Among Myselves", [6, 39, 2]], ["Spineless Jelly", [5, 47, 2]], ["Interstat", [7, 18, 2]],
      ]),
    },
    {
      id: "gnr-appetite", name: "Appetite for Destruction", albumArtist: "Guns N' Roses",
      year: "1987", genre: "Hard Rock", sampleRate: 96, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Guns N' Roses", [
        ["Welcome to the Jungle", [4, 31]], ["It's So Easy", [3, 22]], ["Nightrain", [4, 28]],
        ["Out ta Get Me", [4, 24]], ["Mr. Brownstone", [3, 49]], ["Paradise City", [6, 46]],
        ["My Michelle", [3, 39]], ["Think About You", [3, 50]], ["Sweet Child o' Mine", [5, 56]],
        ["Rocket Queen", [6, 13]],
      ]),
    },
    {
      id: "rh-okc", name: "OK Computer", albumArtist: "Radiohead",
      year: "1997", genre: "Alternative", sampleRate: 96, bitDepth: 24, side: "SIDE A",
      tracks: tracks("Radiohead", [
        ["Airbag", [4, 44]], ["Paranoid Android", [6, 23]], ["Subterranean Homesick Alien", [4, 27]],
        ["Exit Music (For a Film)", [4, 24]], ["Let Down", [4, 59]], ["Karma Police", [4, 21]],
        ["Fitter Happier", [1, 57]], ["Electioneering", [3, 50]], ["Climbing Up the Walls", [4, 45]],
        ["No Surprises", [3, 48]], ["Lucky", [4, 19]], ["The Tourist", [5, 24]],
      ]),
    },
    {
      id: "bd-beethoven", name: "Beethoven: Symphony No. 7", albumArtist: "Daniel Barenboim",
      year: "2012", genre: "Classical", sampleRate: 192, bitDepth: 24, side: "MVT. I",
      tracks: tracks("Wiener Philharmoniker, Daniel Barenboim", [
        ["I. Poco sostenuto – Vivace", [13, 2]], ["II. Allegretto", [9, 18]],
        ["III. Presto", [8, 56]], ["IV. Allegro con brio", [8, 47]],
      ]),
    },
    {
      id: "celine-falling", name: "Falling into You", albumArtist: "Céline Dion",
      year: "1996", genre: "Pop", sampleRate: 44, bitDepth: 16, side: "SIDE A",
      tracks: tracks("Céline Dion", [
        ["It's All Coming Back to Me Now", [7, 38]], ["Because You Loved Me", [4, 34]],
        ["Falling into You", [4, 18]], ["Make You Happy", [4, 17]], ["Seduces Me", [3, 45]],
        ["All by Myself", [5, 12]], ["Declaration of Love", [4, 5]],
        ["The Power of the Dream", [4, 33], 1, "Céline Dion feat. David Foster"],
      ]),
    },
  ];

  // index by album artist
  const byArtist = {};
  albums.forEach((a) => {
    (byArtist[a.albumArtist] = byArtist[a.albumArtist] || []).push(a);
  });
  const artists = Object.keys(byArtist).sort((a, b) =>
    a.replace(/^The /, "").localeCompare(b.replace(/^The /, ""))
  ).map((name) => ({
    name,
    albums: byArtist[name],
    albumCount: byArtist[name].length,
  }));

  const zones = {
    server: [
      { id: "z-living", name: "Living Room", type: "Network Zone", active: true, vol: 0.62 },
      { id: "z-study", name: "Study — DAC", type: "DLNA Renderer", active: false, vol: 0.40 },
      { id: "z-kitchen", name: "Kitchen", type: "Network Zone", active: false, vol: 0.55 },
      { id: "z-deck", name: "Back Deck", type: "Network Zone", active: false, vol: 0.30 },
    ],
    device: [
      { id: "z-this", name: "This Computer", type: "Local Playback", active: false, vol: 0.5 },
    ],
  };

  // Build a starting queue from a couple of albums
  const dsotm = albums.find((a) => a.id === "pf-dsotm");
  const wywh = albums.find((a) => a.id === "pf-wywh");
  const queue = [
    ...dsotm.tracks.map((t) => ({ ...t, album: dsotm.name, albumArtist: dsotm.albumArtist, albumId: dsotm.id })),
    ...wywh.tracks.slice(0, 3).map((t) => ({ ...t, album: wywh.name, albumArtist: wywh.albumArtist, albumId: wywh.id })),
  ];

  window.DATA = { albums, artists, byArtist, zones, queue, dsotm };
})();
