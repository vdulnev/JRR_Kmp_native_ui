# Multi-disc album test examples

Generated from `MCLibrary.xml` by applying the app's `normalizeAlbumName`
regexes, then grouping names that share a base after stripping disc markers.
A "set" = a base name with 2+ disc variants → these SHOULD collapse to one
entry in the artist-albums view. Each variant lists its **album artist**,
**year**, and **full folder path** so you can confirm discs really live under
a common parent vs. when a match is a false positive.

- Distinct multi-disc sets detected: **591**
- Total album-name rows involved: **1627**

---

## Curated by naming pattern

### Standard `(Disc N)` / `(CD N)`
- **090909 Sampler** — 2 variants
    - `090909 Sampler (CD 1)`
        - artist: The Beatles  ·  year: 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/2009 - 09.09.09 Sampler/Disc 1`
    - `090909 Sampler (CD 2)`
        - artist: The Beatles  ·  year: 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/2009 - 09.09.09 Sampler/Disc 2`
- **100 Hits** — 5 variants
    - `100 Hits (Disc 1)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD 1`
    - `100 Hits (Disc 2)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD 2`
    - `100 Hits (Disc 3)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD3`
    - `100 Hits (Disc 4)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD4`
    - `100 Hits (Disc 5)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD5`
- **15 Pieces Of Live Shit: Binge & Purge** — 2 variants
    - `15 Pieces Of Live Shit: Binge & Purge (Disc 1)`
        - artist: Metallica  ·  year: 1993
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1993 - Live Shit - Binge & Purge (3 CD)/1993 15 Pieces Of Live Shit  Binge & Purge (USA Promo 2CD, Elektra PRCD 8879-2)/Disc 1`
    - `15 Pieces Of Live Shit: Binge & Purge (Disc 2)`
        - artist: Metallica  ·  year: 1993
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1993 - Live Shit - Binge & Purge (3 CD)/1993 15 Pieces Of Live Shit  Binge & Purge (USA Promo 2CD, Elektra PRCD 8879-2)/Disc 2`
- **1962 - 1966** — 5 variants
    - `1962 - 1966  (Disc 1)`
        - artist: The Beatles  ·  year: 1993
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966/Disc 1`
    - `1962 - 1966 (Disc 1)`
        - artist: The Beatles  ·  year: 2010
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966 1967 - 1970/1962 - 1966/Disc 1`
    - `1962 - 1966 (Disc 1) (0602455920768)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1962 - 1966/Disc 1`
    - `1962 - 1966 (Disc 2)`
        - artist: The Beatles  ·  year: 1993, 2010
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966/Disc 2`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966 1967 - 1970/1962 - 1966/Disc 2`
    - `1962 - 1966 (Disc 2) (0602455920768)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1962 - 1966/Disc 2`

### Letter discs `(Disc A)` / `(Disc B)`
- **101** — 2 variants
    - `101 (Disc A)`
        - artist: Depeche Mode  ·  year: 1989
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (MUTE INT 892.650) (1st Germany 1989)/Disc A`
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (SIRE 9 25853-2) (1st USA 1989)/Disc A`
    - `101 (Disc B)`
        - artist: Depeche Mode  ·  year: 1989
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (MUTE INT 892.650) (1st Germany 1989)/Disc B`
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (SIRE 9 25853-2) (1st USA 1989)/Disc B`

### Written-out numbers `(Disc One/Two/...)`
- **Anthology** — 2 variants
    - `Anthology (Disc One)`
        - artist: Bryan Adams  ·  year: 2005
        - folder: `D:/music/_cd_rip/Bryan Adams - Discography/2005 - Anthology (2005 - Polydor Limited (UK) - Germany - 987 5798)/CD1`
    - `Anthology (Disc Two)`
        - artist: Bryan Adams  ·  year: 2005
        - folder: `D:/music/_cd_rip/Bryan Adams - Discography/2005 - Anthology (2005 - Polydor Limited (UK) - Germany - 987 5798)/CD2`
- **Finyl Vinyl** — 4 variants
    - `Finyl Vinyl (Disc 1)`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/1986. Rainbow - Finyl Vinyl/1986. Rainbow - Fynil Vinyl (Polydor 547 368-2, Germany)/Disc1`
    - `Finyl Vinyl (Disc Two)`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/1986. Rainbow - Finyl Vinyl/1986. Rainbow - Fynil Vinyl (Polydor 547 368-2, Germany)/Disc2`
    - `Finyl Vinyl (SHM-CD Japanese UICY-93626) Disc 1`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow-(SHM-CD_Japanese_UICY-93618-27)/(SHM-CD_Japanese_UICY-93626-7)-Finyl_Vinyl/CD1`
    - `Finyl Vinyl (SHM-CD Japanese UICY-93627) Disc 2`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow-(SHM-CD_Japanese_UICY-93618-27)/(SHM-CD_Japanese_UICY-93626-7)-Finyl_Vinyl/CD2`
- **GOLD** — 4 variants
    - `GOLD (Disc One)`
        - artist: KISS  ·  year: 2004
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2004 Gold/2004 Gold [2004 Mercury B0003419-00 USA]/CD 1`
    - `GOLD (Disc Two)`
        - artist: KISS  ·  year: 2004
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2004 Gold/2004 Gold [2004 Mercury B0003419-00 USA]/CD 2`
    - `Gold (Disc One)`
        - artist: Cinderella  ·  year: 2006
        - folder: `D:/music/Cinderella -2006- Gold (US. 2CD) [torrents.ru]/CD1`
    - `Gold (Disc Two)`
        - artist: Cinderella  ·  year: 2006
        - folder: `D:/music/Cinderella -2006- Gold (US. 2CD) [torrents.ru]/CD2`
- **In And Out Of Consciousness** — 2 variants
    - `In And Out Of Consciousness (Disc Three)`
        - artist: Robbie Williams  ·  year: 2010
        - folder: `D:/music/_cd_rip/Robbie Williams/2010 - In And Out Of Consciousness (Deluxe Edition)/CD3`
    - `In And Out Of Consciousness (Disc Two)`
        - artist: Robbie Williams  ·  year: 2010
        - folder: `D:/music/_cd_rip/Robbie Williams/2010 - In And Out Of Consciousness (Deluxe Edition)/CD2`

### Two-digit disc numbers `(Disc NN)`
- **Let It Be (MFSL SuperVinyl 24/96)** — 3 variants
    - `Let It Be  (MFSL SuperVinyl 24/96)`
        - artist: The Beatles  ·  year: 1970
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/13. 1970 - Let It Be {24bit.96kHz Vinyl Rip MFSL 1-109}`
    - `Let It Be (Disc 1) (B0032261-02)`
        - artist: The Beatles  ·  year: 2021
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2021 - Let It Be/Let It Be 2CD Edition (Apple B0032261-02)/Disc 1`
    - `Let It Be (Disc 16)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - CD Singles Collection/16 Let It Be`
- **Magical Mystery Tour (Mono Version)** — 2 variants
    - `Magical Mystery Tour (Mono Version) (Disc 14)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/13-14 Magical Mystery Tour/14 Mono Version`
    - `Magical Mystery Tour (Stereo Version) (Disc 13)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/13-14 Magical Mystery Tour/13 Stereo Version`
- **The Beatles (MFSL SuperVinyl 24/96)** — 9 variants
    - `The Beatles   (MFSL SuperVinyl 24/96)`
        - artist: The Beatles  ·  year: 1968
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/10. 1968 - White Album {24bit.96kHz Vinyl Rip MFSL 2-072}/LP1`
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/10. 1968 - White Album {24bit.96kHz Vinyl Rip MFSL 2-072}/LP2`
    - `The Beatles (Disc 1)`
        - artist: The Beatles, Beatles  ·  year: 1998, 1968, 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Apple 7243 4 96895 2 7)/Disc 1`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Toshiba-EMI CP25-5329-30)/Disc 1`
    - `The Beatles (Disc 1) (CDS 7 46443 8)`
        - artist: The Beatles  ·  year: 1987
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Parl. CDS 7 46443 8)/Disc 1`
    - `The Beatles (Disc 15)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/15 The Beatles`
    - `The Beatles (Disc 2)`
        - artist: The Beatles, Beatles, The  ·  year: 1998, 1968, 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Apple 7243 4 96895 2 7)/Disc 2`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Toshiba-EMI CP25-5329-30)/Disc 2`
    - `The Beatles (Disc 2) (CDS 7 46443 8)`
        - artist: The Beatles  ·  year: 1987
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Parl. CDS 7 46443 8)/Disc 2`
    - … (+3 more variants)
- **Weld** — 2 variants
    - `Weld (Disc 02)`
        - artist: Neil Young & Crazy Horse  ·  year: 1991
        - folder: `D:/music/_cd_rip/Neil Young/1991. Neil Young - Weld (Reprise 7599-26671-2, Germany)/Disc 2`
    - `Weld [Disc 1]`
        - artist: Young, Neil & Crazy Horse  ·  year: 1991
        - folder: `D:/music/_cd_rip/Neil Young/1991. Neil Young - Weld (Reprise 7599-26671-2, Germany)/Disc 1`

### No-space `(DiscN)` / `(CDN)`
- **25** — 5 variants
    - `25 (CD 2)`
        - artist: Bad Boys Blue  ·  year: 2011
        - folder: `D:/music/Bad Boys Blue - 25 (Poland Edition) 3CD/CD2`
    - `25 (CD 3)`
        - artist: Bad Boys Blue  ·  year: 2011
        - folder: `D:/music/Bad Boys Blue - 25 (Poland Edition) 3CD/CD3`
    - `25 (CD1)`
        - artist: Bad Boys Blue  ·  year: 2011
        - folder: `D:/music/Bad Boys Blue - 25 (Poland Edition) 3CD/CD1`
    - `25 (The 25th Anniversary Album) CD1`
        - artist: Bad Boys Blue  ·  year: 2010
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2010.25 (2 CD+DVD)/25 (The 25th Anniversary Album) CD1`
    - `25 (The 25th Anniversary Album) CD2`
        - artist: Bad Boys Blue  ·  year: 2010
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2010.25 (2 CD+DVD)/25 (The 25th Anniversary Album) CD2`
- **40 Years - Decades Of Decibels** — 2 variants
    - `40 Years - Decades Of Decibels (Disc1)`
        - artist: Kiss  ·  year: 2014
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2014 40 Years - Decades Of Decibels/2014 40 Years - Decades Of Decibels [2014 Mercury 06025 377 857-1 (1) Germany]/CD 1`
    - `40 Years - Decades Of Decibels (Disc2)`
        - artist: Kiss  ·  year: 2014
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2014 40 Years - Decades Of Decibels/2014 40 Years - Decades Of Decibels [2014 Mercury 06025 377 857-1 (1) Germany]/CD 2`
- **Alchemy – Dire Straits Live** — 2 variants
    - `Alchemy – Dire Straits Live (CD1)`
        - artist: Dire Straits  ·  year: 1984
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, USA]`
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, West Germany for USA] [target]`
    - `Alchemy – Dire Straits Live (CD2)`
        - artist: Dire Straits  ·  year: 1984
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, USA]`
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, West Germany for USA] [target]`
- **Alive II** — 6 variants
    - `Alive II (CD1)`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1997 Mercury 314 532 382-2 USA]/CD 1`
    - `Alive II (CD2)`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1997 Mercury 314 532 382-2 USA]/CD 2`
    - `Alive II (Disc 1)  (UICY-93098)`
        - artist: Kiss  ·  year: 2006
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [2006 Mercury UICY- 93098~9 Japan]/CD 1`
    - `Alive II (Disc 2) (UICY-93099)`
        - artist: Kiss  ·  year: 2006
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [2006 Mercury UICY- 93098~9 Japan]/CD 2`
    - `Alive II CD1`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1987 Casablanca Records 832 417-2 West Germany]/CD 1`
    - `Alive II CD2`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1987 Casablanca Records 832 417-2 West Germany]/CD 2`

### Dash style `(... - Disc N)`
- **BackTracks (Deluxe Collector's Edition) - of 3** — 3 variants
    - `BackTracks (Deluxe Collector's Edition) - Disc 1 of 3`
        - artist: AC/DC  ·  year: 2009
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Box-Sets/2009 - Backtracks (COLUMBIA 88697540982 US)`
    - `BackTracks (Deluxe Collector's Edition) - Disc 2 of 3`
        - artist: AC/DC  ·  year: 2009
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Box-Sets/2009 - Backtracks (COLUMBIA 88697540982 US)`
    - `BackTracks (Deluxe Collector's Edition) - Disc 3 of 3`
        - artist: AC/DC  ·  year: 2009
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Box-Sets/2009 - Backtracks (COLUMBIA 88697540982 US)`
- **BBC Sessions** — 4 variants
    - `BBC Sessions (CD 1)`
        - artist: Led Zeppelin  ·  year: 1997
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic 7567-83061-2, Germany)/disc1`
    - `BBC Sessions (CD 2)`
        - artist: Led Zeppelin  ·  year: 1997
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic 7567-83061-2, Germany)/disc2`
    - `BBC Sessions - Disc 1`
        - artist: Led Zeppelin  ·  year: 1997
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic AMCY 2401-2, Japan)/disc1`
    - `BBC Sessions [Disk 2]`
        - artist: Led Zeppelin  ·  year: 1969
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic AMCY 2401-2, Japan)/disc2`
- **Biograph** — 2 variants
    - `Biograph (Disc 1)`
        - artist: Bob Dylan  ·  year: 1985
        - folder: `D:/music/_cd_rip/Dylan/Compilations/1985. Bob Dylan - Biograph (Columbia 88697 85648 2, EU)/Disc1`
    - `Biograph - Disc 2`
        - artist: Bob Dylan  ·  year: 1985
        - folder: `D:/music/_cd_rip/Dylan/Compilations/1985. Bob Dylan - Biograph (Columbia 88697 85648 2, EU)/Disc2`
- **Bob Dylan Bob Dylan Live 1975: The Rolling Thunder Revue** — 2 variants
    - `Bob Dylan   Bob Dylan Live 1975: The Rolling Thunder Revue - Disc 1`
        - artist: Bob Dylan  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2002. Bob Dylan - The Bootleg Series Vol.5 Live 1975 (Columbia-Legacy 88697732902, EU)/Disc1`
    - `Bob Dylan   Bob Dylan Live 1975: The Rolling Thunder Revue - Disc 2`
        - artist: Bob Dylan  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2002. Bob Dylan - The Bootleg Series Vol.5 Live 1975 (Columbia-Legacy 88697732902, EU)/Disc2`

### Bracket style `[ Disc N ]`
- **Before the Flood** — 3 variants
    - `Before the Flood  (CD 1)`
        - artist: Bob Dylan & The Band  ·  year: 1974
        - folder: `D:/music/_cd_rip/Dylan/Live/1974 - Before The Flood/1974. Bob Dylan - Before The Flood (Columbia CD 22137, Austria)/Disc1`
    - `Before the Flood (CD 2)`
        - artist: Bob Dylan & The Band  ·  year: 1974
        - folder: `D:/music/_cd_rip/Dylan/Live/1974 - Before The Flood/1974. Bob Dylan - Before The Flood (Columbia CD 22137, Austria)/Disc2`
    - `Before the Flood [disc 2]`
        - artist: Bob Dylan & The Band  ·  year: 1974
        - folder: `D:/music/_cd_rip/Dylan/Live/1974 - Before The Flood/1974. Bob Dylan - Before The Flood (Columbia 88691924312.16&17, EU)/Disc2`
- **Decade** — 2 variants
    - `Decade (Disc 1)`
        - artist: Neil Young  ·  year: 1977
        - folder: `D:/music/_cd_rip/Neil Young/1977. Neil Young - Decade (2 CD) (2002 Reprise Records, 7599-27233-2, Germany)/Disc One`
    - `Decade [Disc 2]`
        - artist: Neil Young  ·  year: 1977
        - folder: `D:/music/_cd_rip/Neil Young/1977. Neil Young - Decade (2 CD) (2002 Reprise Records, 7599-27233-2, Germany)/Disc Two`
- **Hampton Coliseum - Live in 1981** — 2 variants
    - `Hampton Coliseum - Live in 1981 [disc 1]`
        - artist: Rolling Stones  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2012 Hampton Coliseum (Live In 1981) [Promotone EAGDV037]/Disc 1`
    - `Hampton Coliseum - Live in 1981 [disc 2]`
        - artist: Rolling Stones  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2012 Hampton Coliseum (Live In 1981) [Promotone EAGDV037]/Disc 2`
- **Heartbreak Station [PHDR-18]** — 3 variants
    - `Heartbreak Station (CDS) [PHDR-18]`
        - artist: Cinderella  ·  year: 1991
        - folder: `D:/music/Cinderella/1991 Heartbreak Station (CDS)`
    - `Heartbreak Station [disc 01]`
        - artist: Cinderella  ·  year: 2011
        - folder: `D:/music/Cinderella - 1990 - Heartbreak Station {2011 Remaster BAD110601}/disc 01`
    - `Heartbreak Station [disc 02]`
        - artist: Cinderella  ·  year: 2011
        - folder: `D:/music/Cinderella - 1990 - Heartbreak Station {2011 Remaster BAD110601}/disc 02`

### Mid-name / trailing `CD1` (no parens)
- **2010 - Compilation - Influence** — 2 variants
    - `2010 - Compilation - Influence CD1`
        - artist: Art of Noise  ·  year: 2010
        - folder: `D:/music/AON 2010 - Influence (2CD Element Edition)/CD1 The A-Side - Singles, Hits, Soundtracks And Collaborations`
    - `2010 - Compilation - Influence CD2`
        - artist: Art of Noise  ·  year: 2010
        - folder: `D:/music/AON 2010 - Influence (2CD Element Edition)/CD2 The AA Side - Unreleased Experiments, Before And After Science`
- **40** — 2 variants
    - `40 CD1`
        - artist: Foreigner  ·  year: 2017
        - folder: `D:/music/_cd_rip/Foreigner/Compilation/2017. Foreigner - 40 (Rhino 081227935306, EU)/Disc 1`
    - `40 CD2`
        - artist: Foreigner  ·  year: 2017
        - folder: `D:/music/_cd_rip/Foreigner/Compilation/2017. Foreigner - 40 (Rhino 081227935306, EU)/Disc 2`
- **A Little South Of Sanity [GEFD2-25314]** — 2 variants
    - `A Little South Of Sanity [GEFD2-25314] CD1`
        - artist: Aerosmith  ·  year: 1998
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Lives/1998 - A Little South of Sanity/1998 - A Little South of Sanity (GEFD2-25314) USA/CD 1`
    - `A Little South Of Sanity [GEFD2-25314] CD2`
        - artist: Aerosmith  ·  year: 1998
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Lives/1998 - A Little South of Sanity/1998 - A Little South of Sanity (GEFD2-25314) USA/CD 2`
- **A Woman & A Man (Remastered & Expanded Special Edition)** — 2 variants
    - `A Woman & A Man (Remastered & Expanded Special Edition) CD1`
        - artist: Belinda Carlisle  ·  year: 1996
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1996 A Woman & A Man (2014, Edsel Records, EDSG 8046)/CD1`
    - `A Woman & A Man (Remastered & Expanded Special Edition) CD2`
        - artist: Belinda Carlisle  ·  year: 1996
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1996 A Woman & A Man (2014, Edsel Records, EDSG 8046)/CD2`

### Disc marker + extra release/catalog block
- **1967 - 1970** — 4 variants
    - `1967 - 1970 (Disc 1)`
        - artist: The Beatles  ·  year: 1993
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1967 - 1970/Disc 1`
    - `1967 - 1970 (Disc 1) (0602455920959)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1967 - 1970/Disc 1`
    - `1967 - 1970 (Disc 2)`
        - artist: The Beatles  ·  year: 2010, 1993
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966 1967 - 1970/1967 - 1970/Disc 2`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1967 - 1970/Disc 2`
    - `1967 - 1970 (Disc 2) (0602455920959)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1967 - 1970/Disc 2`
- **Abbey Road (B0030718-02)** — 2 variants
    - `Abbey Road (Disc 1) (B0030718-02)`
        - artist: The Beatles  ·  year: 2019
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2019 - Abbey Road/Abbey Road (2CD Deluxe Edition)/Disc 1`
    - `Abbey Road (Disc 2) (B0030718-02)`
        - artist: The Beatles  ·  year: 2019
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2019 - Abbey Road/Abbey Road (2CD Deluxe Edition)/Disc 2`
- **Alive! (UICY-93094)** — 3 variants
    - `Alive! (Disc 2) (UICY-93094)`
        - artist: Kiss  ·  year: 2006
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [2006 Mercury UICY-93093~4 Japan]/CD 2`
    - `Alive! CD1`
        - artist: Kiss  ·  year: 1975
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1995 Casablanca Records 822 780-2 Australia]/CD1`
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1997 Mercury 532 377-2 Germany]/CD 1`
    - `Alive! CD2`
        - artist: Kiss  ·  year: 1975
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1995 Casablanca Records 822 780-2 Australia]/CD2`
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1997 Mercury 532 377-2 Germany]/CD 2`
- **All Things Must Pass** — 6 variants
    - `All Things Must Pass  (CD1)`
        - artist: George Harrison  ·  year: 1970
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (Capitol CDP 7 46688 2)/Disc 1`
    - `All Things Must Pass (CD 2)`
        - artist: George Harrison  ·  year: 1970
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (Capitol CDP 7 46688 2)/Disc 2`
    - `All Things Must Pass (Disc 1) (0602537914036)`
        - artist: George Harrison  ·  year: 2014
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/05 Box Sets/2014 The Apple Years 1968-75 (G.H. Estate 0602537913879)/1970 All Things Must Pass/Disc 1`
    - `All Things Must Pass (Disc 1) (7243 530474 2 9)`
        - artist: George Harrison  ·  year: 2001
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (GnOM 7243 530474 2 9)/Disc 1`
    - `All Things Must Pass (Disc 2) (0602537914081)`
        - artist: George Harrison  ·  year: 2014
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/05 Box Sets/2014 The Apple Years 1968-75 (G.H. Estate 0602537913879)/1970 All Things Must Pass/Disc 2`
    - `All Things Must Pass (Disc 2) (7243 530474 2 9)`
        - artist: George Harrison  ·  year: 2001
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (GnOM 7243 530474 2 9)/Disc 2`

### Cyrillic `(Диск N)`
- **Играйте дома [2005, Отделение Выход, В 43]** — 2 variants
    - `Играйте дома (Диск A) [2005, Отделение Выход, В 43]`
        - artist: Зоопарк  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984-85 - Майк - Играйте дома [2005, В 43_44]`
    - `Играйте дома (Диск B) [2005, Отделение Выход, В 44]`
        - artist: Зоопарк  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984-85 - Майк - Играйте дома [2005, В 43_44]`
- **Ленинград 1984 [2009, Отделение Выход, В 139]** — 2 variants
    - `Ленинград 1984 (Диск 1) [2009, Отделение Выход, В 139]`
        - artist: Зоопарк  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984 - Майк и Цой - Ленинград 1984 [2009, 2CD, В 139-140]`
    - `Ленинград 1984 (Диск 2) [2009, Отделение Выход, В 140]`
        - artist: Майк и Цой  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984 - Майк и Цой - Ленинград 1984 [2009, 2CD, В 139-140]`
- **Москва 1985 [2012, Отделение Выход, В 329]** — 2 variants
    - `Москва 1985 (Диск 1) [2012, Отделение Выход, В 329]`
        - artist: Зоопарк  ·  year: 1985
        - folder: `D:/music/Зоопарк/2. Концертные записи/1985 - Майк и Цой - Москва 1985 [2012, В 329_330]`
    - `Москва 1985 (Диск 2) [2012, Отделение Выход, В 330]`
        - artist: Зоопарк  ·  year: 1985
        - folder: `D:/music/Зоопарк/2. Концертные записи/1985 - Майк и Цой - Москва 1985 [2012, В 329_330]`
- **Сладкая N и другие** — 3 variants
    - `Сладкая N  и другие`
        - artist: Зоопарк  ·  year: 1980
        - folder: `D:/music/Зоопарк - Майк Науменко/1980. Майк  - Сладкая N  и другие`
    - `Сладкая N и другие (Диск 1) [2001, Отделение Выход, В 134]`
        - artist: Майк  ·  year: 1980
        - folder: `D:/music/Зоопарк/1. Альбомы/1980 - Майк - Сладкая N и другие/1980 - Майк - Сладкая N и другие [2001, 2CD, В 134-135]`
    - `Сладкая N и другие (Диск 2) [2001, Отделение Выход, В 135]`
        - artist: Майк  ·  year: 1980
        - folder: `D:/music/Зоопарк/1. Альбомы/1980 - Майк - Сладкая N и другие/1980 - Майк - Сладкая N и другие [2001, 2CD, В 134-135]`

## Largest sets (4+ discs)

- **Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive** — 36 variants  ·  Richie Havens, Sri Swami Satchidananda, John Morris
- **The Casablanca Singles 1974-1982** — 29 variants  ·  Kiss
- **The Wall** — 18 variants  ·  Pink Floyd
- **Garage Inc. [2013 Blackened BLCKND013-2]** — 15 variants  ·  Metallica
- **Physical Graffiti** — 13 variants  ·  Led Zeppelin
- **Live In London (2003, SPV, SPV 092-74262 DCD-E, Germany)** — 10 variants  ·  Judas Priest
- **S&M [Germany Vertigo 546 797-2]** — 10 variants  ·  Metallica
- **The Beatles (MFSL SuperVinyl 24/96)** — 9 variants  ·  The Beatles
- **The Essential** — 8 variants  ·  Leonard Cohen
- **The Ultimate Collection** — 8 variants  ·  Sade
- **The Fame Monster (Japanese Deluxe Edition)** — 8 variants  ·  Lady GaGa
- **Double Live Assassins** — 8 variants  ·  W.A.S.P.
- **The Song Remains The Same** — 8 variants  ·  Led Zeppelin
- **Love You Live [CBS]** — 8 variants  ·  The Rolling Stones
- **Jewel Box** — 8 variants  ·  Elton John, Elton John / Leon Russell, Little Richard & Elton John

---

## Negative cases — must NOT group (CD = catalog/year, single disc)

- `A Hard Day's Night (CDP 7 46437 2)`  ·  The Beatles  ·  1987
- `A Momentary Lapse Of Reason (CDP 7 48068 2)`  ·  Pink Floyd  ·  1987
- `A Saucerful Of Secrets (CDP 7 46383 2)`  ·  Pink Floyd  ·  1968
- `Abbey Road (CDP 7 46446 2)`  ·  The Beatles  ·  1987
- `Ace Of Spades (1996, UK, Castle, ESM CD 312)`  ·  Motorhead  ·  1980
- `Animal Magnetism (CDP 7 46734 2)`  ·  Scorpions  ·  1980
- `Another Perfect Day (1996, UK, Castle, ESM CD 438)`  ·  Motorhead  ·  1983
- `Anthology (VSOP CD 245)`  ·  Dio  ·  1997
- `Anthology Volume Two (VSOP CD 338)`  ·  Dio  ·  2001
- `Atom Heart Mother (CDP 746381 2, 1st West Germany)`  ·  Pink Floyd  ·  1970
- `Back In Black (Albert CD 431046)`  ·  AC/DC  ·  1980
- `Battle Hymns (CDP 538 7 91989 2)`  ·  Manowar  ·  1982
- `Beatles For Sale (CDP 7 46438 2)`  ·  The Beatles  ·  1987
- `Between Heaven And Hell 1970-1983 (RAW CD 104)`  ·  Black Sabbath  ·  1995
- `Birdy (CAS CD 1167)`  ·  Peter Gabriel  ·  1985
- `Black Sabbath (ESM CD 301)`  ·  Black Sabbath  ·  1970
- `Blackout (CDP 7 46732 2)`  ·  Scorpions  ·  1982
- `Blow Up Your Video (Albert CDP 748977 2)`  ·  AC/DC  ·  1988
- `Bomber (1996, UK, Castle, ESM CD 311)`  ·  Motorhead  ·  1979
- `Born Again (ESM CD 334)`  ·  Black Sabbath  ·  1983
- `Burn (1989, CDP 7 92611 2)`  ·  Deep Purple  ·  1973
- `Christmas CD 1996`  ·  Dream Theater  ·  1996
- `Christmas CD 1997: The Making of Falling Into Infinity`  ·  Dream Theater  ·  2009
- `Christmas CD 1998: Once In A Livetime Outtakes`  ·  Dream Theater  ·  1998
- `Christmas CD 1999: Cleaning Out The Closet`  ·  Dream Theater  ·  1999

---

## Appendix — all 591 detected multi-disc sets

### '98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany)  (4)
    - `'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany, CD 1)`
        - artist: Judas Priest  ·  year: 1998
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1998 - '98 Live Meltdown/1998 - '98 Live Meltdown [SPV, SPV 089-18542 CD, Germany]`
    - `'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany, CD 2)`
        - artist: Judas Priest  ·  year: 1998
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1998 - '98 Live Meltdown/1998 - '98 Live Meltdown [SPV, SPV 089-18542 CD, Germany]`
    - `'98 Live Meltdown (1998, Zero / Toshiba, XRCN-2039, Japan, CD 1)`
        - artist: Judas Priest  ·  year: 1998
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1998 - '98 Live Meltdown/1998 - '98 Live Meltdown [Zero-Toshiba, XRCN-2039~40, Japan]`
    - `'98 Live Meltdown (1998, Zero / Toshiba, XRCN-2040, Japan, CD 2)`
        - artist: Judas Priest  ·  year: 1998
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1998 - '98 Live Meltdown/1998 - '98 Live Meltdown [Zero-Toshiba, XRCN-2039~40, Japan]`

### ...To The Rising Sun. In Tokyo (2015, 2CD+DVD, 0210547EMU)  (2)
    - `...To The Rising Sun. In Tokyo CD1 (2015, 2CD+DVD, 0210547EMU)`
        - artist: Deep Purple  ·  year: 2015
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2015. ...To The Rising Sun. In Tokyo (2015, 2CD+DVD, Edel, Germany, 0210547EMU)/CD1`
    - `...To The Rising Sun. In Tokyo CD2 (2015, 2CD+DVD, 0210547EMU)`
        - artist: Deep Purple  ·  year: 2015
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2015. ...To The Rising Sun. In Tokyo (2015, 2CD+DVD, Edel, Germany, 0210547EMU)/CD2`

### 090909 Sampler  (2)
    - `090909 Sampler (CD 1)`
        - artist: The Beatles  ·  year: 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/2009 - 09.09.09 Sampler/Disc 1`
    - `090909 Sampler (CD 2)`
        - artist: The Beatles  ·  year: 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/2009 - 09.09.09 Sampler/Disc 2`

### 100 Hits  (5)
    - `100 Hits (Disc 1)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD 1`
    - `100 Hits (Disc 2)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD 2`
    - `100 Hits (Disc 3)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD3`
    - `100 Hits (Disc 4)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD4`
    - `100 Hits (Disc 5)`
        - artist: Tom Jones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2012 - 100 hits (5 CD)/CD5`

### 100,000,000 BON JOVI Fans...  (4)
    - `100,000,000 BON JOVI Fans...CD01`
        - artist: Bon Jovi  ·  year: 2004
        - folder: `D:/music/_cd_rip/BJ_Discography/04 Box set/2004-100,000,000 BON JOVI Fans Can't Be Wrong/CD01`
    - `100,000,000 BON JOVI Fans...CD02`
        - artist: Bon Jovi  ·  year: 2004
        - folder: `D:/music/_cd_rip/BJ_Discography/04 Box set/2004-100,000,000 BON JOVI Fans Can't Be Wrong/CD02`
    - `100,000,000 BON JOVI Fans...CD03`
        - artist: Bon Jovi  ·  year: 2004
        - folder: `D:/music/_cd_rip/BJ_Discography/04 Box set/2004-100,000,000 BON JOVI Fans Can't Be Wrong/CD03`
    - `100,000,000 BON JOVI Fans...CD04`
        - artist: Bon Jovi  ·  year: 2004
        - folder: `D:/music/_cd_rip/BJ_Discography/04 Box set/2004-100,000,000 BON JOVI Fans Can't Be Wrong/CD04`

### 101  (2)
    - `101 (Disc A)`
        - artist: Depeche Mode  ·  year: 1989
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (MUTE INT 892.650) (1st Germany 1989)/Disc A`
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (SIRE 9 25853-2) (1st USA 1989)/Disc A`
    - `101 (Disc B)`
        - artist: Depeche Mode  ·  year: 1989
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (MUTE INT 892.650) (1st Germany 1989)/Disc B`
        - folder: `D:/music/Depeche Mode/10. Depeche Mode - 101 (1989)/Depeche Mode - 101 (1989) (SIRE 9 25853-2) (1st USA 1989)/Disc B`

### 13 (Deluxe Edition, 3735427)  (2)
    - `13 (Deluxe Edition, 3735427, CD1)`
        - artist: Black Sabbath  ·  year: 2013
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/2013 13/2013 13 [2013 EU 3735427 Universal]/CD1`
    - `13 (Deluxe Edition, 3735427, CD2)`
        - artist: Black Sabbath  ·  year: 2013
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/2013 13/2013 13 [2013 EU 3735427 Universal]/CD2`

### 15 Pieces Of Live Shit: Binge & Purge  (2)
    - `15 Pieces Of Live Shit: Binge & Purge (Disc 1)`
        - artist: Metallica  ·  year: 1993
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1993 - Live Shit - Binge & Purge (3 CD)/1993 15 Pieces Of Live Shit  Binge & Purge (USA Promo 2CD, Elektra PRCD 8879-2)/Disc 1`
    - `15 Pieces Of Live Shit: Binge & Purge (Disc 2)`
        - artist: Metallica  ·  year: 1993
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1993 - Live Shit - Binge & Purge (3 CD)/1993 15 Pieces Of Live Shit  Binge & Purge (USA Promo 2CD, Elektra PRCD 8879-2)/Disc 2`

### 1962 - 1966  (5)
    - `1962 - 1966  (Disc 1)`
        - artist: The Beatles  ·  year: 1993
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966/Disc 1`
    - `1962 - 1966 (Disc 1)`
        - artist: The Beatles  ·  year: 2010
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966 1967 - 1970/1962 - 1966/Disc 1`
    - `1962 - 1966 (Disc 1) (0602455920768)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1962 - 1966/Disc 1`
    - `1962 - 1966 (Disc 2)`
        - artist: The Beatles  ·  year: 1993, 2010
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966/Disc 2`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966 1967 - 1970/1962 - 1966/Disc 2`
    - `1962 - 1966 (Disc 2) (0602455920768)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1962 - 1966/Disc 2`

### 1967 - 1970  (4)
    - `1967 - 1970 (Disc 1)`
        - artist: The Beatles  ·  year: 1993
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1967 - 1970/Disc 1`
    - `1967 - 1970 (Disc 1) (0602455920959)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1967 - 1970/Disc 1`
    - `1967 - 1970 (Disc 2)`
        - artist: The Beatles  ·  year: 2010, 1993
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1962 - 1966 1967 - 1970/1967 - 1970/Disc 2`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1973 - 1962-1966 1967-1970/1967 - 1970/Disc 2`
    - `1967 - 1970 (Disc 2) (0602455920959)`
        - artist: The Beatles  ·  year: 2023
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2023 - 1967 - 1970/Disc 2`

### 2010 - Compilation - Influence  (2)
    - `2010 - Compilation - Influence CD1`
        - artist: Art of Noise  ·  year: 2010
        - folder: `D:/music/AON 2010 - Influence (2CD Element Edition)/CD1 The A-Side - Singles, Hits, Soundtracks And Collaborations`
    - `2010 - Compilation - Influence CD2`
        - artist: Art of Noise  ·  year: 2010
        - folder: `D:/music/AON 2010 - Influence (2CD Element Edition)/CD2 The AA Side - Unreleased Experiments, Before And After Science`

### 25  (5)
    - `25 (CD 2)`
        - artist: Bad Boys Blue  ·  year: 2011
        - folder: `D:/music/Bad Boys Blue - 25 (Poland Edition) 3CD/CD2`
    - `25 (CD 3)`
        - artist: Bad Boys Blue  ·  year: 2011
        - folder: `D:/music/Bad Boys Blue - 25 (Poland Edition) 3CD/CD3`
    - `25 (CD1)`
        - artist: Bad Boys Blue  ·  year: 2011
        - folder: `D:/music/Bad Boys Blue - 25 (Poland Edition) 3CD/CD1`
    - `25 (The 25th Anniversary Album) CD1`
        - artist: Bad Boys Blue  ·  year: 2010
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2010.25 (2 CD+DVD)/25 (The 25th Anniversary Album) CD1`
    - `25 (The 25th Anniversary Album) CD2`
        - artist: Bad Boys Blue  ·  year: 2010
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2010.25 (2 CD+DVD)/25 (The 25th Anniversary Album) CD2`

### 30: Very Best Of (1998, 2CD, 724349680821)  (2)
    - `30: Very Best Of CD1 (1998, 2CD, 724349680821)`
        - artist: Deep Purple  ·  year: 1998
        - folder: `D:/music/_cd_rip/Deep Purple/Compilation Albums/1998. 30. Very Best Of (1998, 2CD, EMI, EU Poland, 724349680821)/CD1`
    - `30: Very Best Of CD2 (1998, 2CD, 724349680821)`
        - artist: Deep Purple  ·  year: 1998
        - folder: `D:/music/_cd_rip/Deep Purple/Compilation Albums/1998. 30. Very Best Of (1998, 2CD, EMI, EU Poland, 724349680821)/CD2`

### 40  (2)
    - `40 CD1`
        - artist: Foreigner  ·  year: 2017
        - folder: `D:/music/_cd_rip/Foreigner/Compilation/2017. Foreigner - 40 (Rhino 081227935306, EU)/Disc 1`
    - `40 CD2`
        - artist: Foreigner  ·  year: 2017
        - folder: `D:/music/_cd_rip/Foreigner/Compilation/2017. Foreigner - 40 (Rhino 081227935306, EU)/Disc 2`

### 40 Years - Decades Of Decibels  (2)
    - `40 Years - Decades Of Decibels (Disc1)`
        - artist: Kiss  ·  year: 2014
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2014 40 Years - Decades Of Decibels/2014 40 Years - Decades Of Decibels [2014 Mercury 06025 377 857-1 (1) Germany]/CD 1`
    - `40 Years - Decades Of Decibels (Disc2)`
        - artist: Kiss  ·  year: 2014
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2014 40 Years - Decades Of Decibels/2014 40 Years - Decades Of Decibels [2014 Mercury 06025 377 857-1 (1) Germany]/CD 2`

### A Hard Day's Night (MFSL Vinyl Box)  (2)
    - `A Hard Day's Night  (MFSL Vinyl Box)`
        - artist: The Beatles  ·  year: 1964
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/03. 1964 - A Hard Day's Night {24bit.96kHz Vinyl Rip MFSL 1-103}`
    - `A Hard Day's Night (Disc 7)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - CD Singles Collection/07 A Hard Day's Night`

### A Light In The Black 1975-1984 (1975-1976, 5348723)  (5)
    - `A Light In The Black 1975-1984 CD1 (1975-1976, 5348723)`
        - artist: Rainbow  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/2014. Rainbow - A Light In The Black 1975-1984/2014. Rainbow - A Light In The Black 1975-1984 (Polydor, Universal, EU, UK, 5348722)/CD1`
    - `A Light In The Black 1975-1984 CD2 (1977-1978, 5348723)`
        - artist: Rainbow  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/2014. Rainbow - A Light In The Black 1975-1984/2014. Rainbow - A Light In The Black 1975-1984 (Polydor, Universal, EU, UK, 5348722)/CD2`
    - `A Light In The Black 1975-1984 CD3 (1979-1980, 5348725)`
        - artist: Rainbow  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/2014. Rainbow - A Light In The Black 1975-1984/2014. Rainbow - A Light In The Black 1975-1984 (Polydor, Universal, EU, UK, 5348722)/CD3`
    - `A Light In The Black 1975-1984 CD4 (1981-1982, 5348726)`
        - artist: Rainbow  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/2014. Rainbow - A Light In The Black 1975-1984/2014. Rainbow - A Light In The Black 1975-1984 (Polydor, Universal, EU, UK, 5348722)/CD4`
    - `A Light In The Black 1975-1984 CD5 (1983-1984, 5348727)`
        - artist: Rainbow  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/2014. Rainbow - A Light In The Black 1975-1984/2014. Rainbow - A Light In The Black 1975-1984 (Polydor, Universal, EU, UK, 5348722)/CD5`

### A Little South Of Sanity [GEFD2-25314]  (2)
    - `A Little South Of Sanity [GEFD2-25314] CD1`
        - artist: Aerosmith  ·  year: 1998
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Lives/1998 - A Little South of Sanity/1998 - A Little South of Sanity (GEFD2-25314) USA/CD 1`
    - `A Little South Of Sanity [GEFD2-25314] CD2`
        - artist: Aerosmith  ·  year: 1998
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Lives/1998 - A Little South of Sanity/1998 - A Little South of Sanity (GEFD2-25314) USA/CD 2`

### A Reality Tour  (2)
    - `A Reality Tour (Disc 1)`
        - artist: David Bowie  ·  year: 2009
        - folder: `D:/music/_cd_rip/David Bowie/2009 - A Reality Tour/CD1`
    - `A Reality Tour (Disc 2)`
        - artist: David Bowie  ·  year: 2009
        - folder: `D:/music/_cd_rip/David Bowie/2009 - A Reality Tour/CD2`

### A Woman & A Man (Remastered & Expanded Special Edition)  (2)
    - `A Woman & A Man (Remastered & Expanded Special Edition) CD1`
        - artist: Belinda Carlisle  ·  year: 1996
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1996 A Woman & A Man (2014, Edsel Records, EDSG 8046)/CD1`
    - `A Woman & A Man (Remastered & Expanded Special Edition) CD2`
        - artist: Belinda Carlisle  ·  year: 1996
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1996 A Woman & A Man (2014, Edsel Records, EDSG 8046)/CD2`

### Abbey Road (B0030718-02)  (2)
    - `Abbey Road (Disc 1) (B0030718-02)`
        - artist: The Beatles  ·  year: 2019
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2019 - Abbey Road/Abbey Road (2CD Deluxe Edition)/Disc 1`
    - `Abbey Road (Disc 2) (B0030718-02)`
        - artist: The Beatles  ·  year: 2019
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2019 - Abbey Road/Abbey Road (2CD Deluxe Edition)/Disc 2`

### Abbey Road Sessions  (2)
    - `Abbey Road CD2 Sessions`
        - artist: The Beatles  ·  year: 2019
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2019 - Abbey Road/Abbey Road (3CD Super Deluxe Edition )/CD2 Sessions`
    - `Abbey Road CD3 Sessions`
        - artist: The Beatles  ·  year: 2019
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2019 - Abbey Road/Abbey Road (3CD Super Deluxe Edition )/CD3 Sessions`

### Absent Lovers: Live In Montreal 1984 [DGM9804]  (2)
    - `Absent Lovers: Live In Montreal 1984 [DGM9804] CD1`
        - artist: King Crimson  ·  year: 1998
        - folder: `D:/music/King Crimson/3.Live/1998. Absent Lovers/1998, UK, Discipline Global Mobile, DGM9804, Mispress/CD 1`
        - folder: `D:/music/King Crimson/3.Live/1998. Absent Lovers/1998, UK, Discipline Global Mobile, DGM9804/CD 1`
    - `Absent Lovers: Live In Montreal 1984 [DGM9804] CD2`
        - artist: King Crimson  ·  year: 1998
        - folder: `D:/music/King Crimson/3.Live/1998. Absent Lovers/1998, UK, Discipline Global Mobile, DGM9804/CD 2`
        - folder: `D:/music/King Crimson/3.Live/1998. Absent Lovers/1998, UK, Discipline Global Mobile, DGM9804, Mispress/CD 2`

### Ace Of Spades (2008, 0602517855687)  (2)
    - `Ace Of Spades (2008, 0602517855687, CD1)`
        - artist: Motorhead  ·  year: 1980
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1980-Ace Of Spades/1980-Ace Of Spades (2008, EU, Sanctuary, 0602517855687, 2CD)/CD1`
    - `Ace Of Spades (2008, 0602517855687, CD2)`
        - artist: Motorhead  ·  year: 1980
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1980-Ace Of Spades/1980-Ace Of Spades (2008, EU, Sanctuary, 0602517855687, 2CD)/CD2`

### Aftermath (2nd West German Pressing)  (2)
    - `Aftermath (2nd  West German Pressing)`
        - artist: The Rolling Stones  ·  year: 1966
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/08. 1966 Aftermath/Aftermath UK [London 820050-2]`
    - `Aftermath (UK)  [ABKCO 8822952]`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/08. 1966 Aftermath/Aftermath UK  [ABKCO 8822952]`

### Alchemy – Dire Straits Live  (2)
    - `Alchemy – Dire Straits Live (CD1)`
        - artist: Dire Straits  ·  year: 1984
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, USA]`
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, West Germany for USA] [target]`
    - `Alchemy – Dire Straits Live (CD2)`
        - artist: Dire Straits  ·  year: 1984
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, USA]`
        - folder: `D:/music/_cd_rip/(1978-1998) Dire Straits - Дискография (98CD)/Live/(1984) Alchemy – Dire Straits Live [Warner Bros. Records, 9 25085-2, West Germany for USA] [target]`

### Alive II  (6)
    - `Alive II (CD1)`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1997 Mercury 314 532 382-2 USA]/CD 1`
    - `Alive II (CD2)`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1997 Mercury 314 532 382-2 USA]/CD 2`
    - `Alive II (Disc 1)  (UICY-93098)`
        - artist: Kiss  ·  year: 2006
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [2006 Mercury UICY- 93098~9 Japan]/CD 1`
    - `Alive II (Disc 2) (UICY-93099)`
        - artist: Kiss  ·  year: 2006
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [2006 Mercury UICY- 93098~9 Japan]/CD 2`
    - `Alive II CD1`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1987 Casablanca Records 832 417-2 West Germany]/CD 1`
    - `Alive II CD2`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1977 Alive II/1977 Alive II [1987 Casablanca Records 832 417-2 West Germany]/CD 2`

### Alive! (UICY-93094)  (3)
    - `Alive! (Disc 2) (UICY-93094)`
        - artist: Kiss  ·  year: 2006
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [2006 Mercury UICY-93093~4 Japan]/CD 2`
    - `Alive! CD1`
        - artist: Kiss  ·  year: 1975
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1995 Casablanca Records 822 780-2 Australia]/CD1`
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1997 Mercury 532 377-2 Germany]/CD 1`
    - `Alive! CD2`
        - artist: Kiss  ·  year: 1975
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1995 Casablanca Records 822 780-2 Australia]/CD2`
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Live Albums/1975 Alive!/1975 Alive! [1997 Mercury 532 377-2 Germany]/CD 2`

### All The Best  (2)
    - `All The Best (CD 1)`
        - artist: Tina Turner  ·  year: 2004
        - folder: `D:/music/_cd_rip/Tina Turner/2004 - TT_All The Best_(2 CD)/All The Best_(CD1)`
    - `All The Best (CD 2)`
        - artist: Tina Turner  ·  year: 2004
        - folder: `D:/music/_cd_rip/Tina Turner/2004 - TT_All The Best_(2 CD)/All The Best (CD2)`

### All Things Must Pass  (6)
    - `All Things Must Pass  (CD1)`
        - artist: George Harrison  ·  year: 1970
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (Capitol CDP 7 46688 2)/Disc 1`
    - `All Things Must Pass (CD 2)`
        - artist: George Harrison  ·  year: 1970
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (Capitol CDP 7 46688 2)/Disc 2`
    - `All Things Must Pass (Disc 1) (0602537914036)`
        - artist: George Harrison  ·  year: 2014
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/05 Box Sets/2014 The Apple Years 1968-75 (G.H. Estate 0602537913879)/1970 All Things Must Pass/Disc 1`
    - `All Things Must Pass (Disc 1) (7243 530474 2 9)`
        - artist: George Harrison  ·  year: 2001
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (GnOM 7243 530474 2 9)/Disc 1`
    - `All Things Must Pass (Disc 2) (0602537914081)`
        - artist: George Harrison  ·  year: 2014
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/05 Box Sets/2014 The Apple Years 1968-75 (G.H. Estate 0602537913879)/1970 All Things Must Pass/Disc 2`
    - `All Things Must Pass (Disc 2) (7243 530474 2 9)`
        - artist: George Harrison  ·  year: 2001
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/01 Studio Albums/1970 All Things Must Pass/All Things Must Pass (GnOM 7243 530474 2 9)/Disc 2`

### Amaranth  (4)
    - `Amaranth (CDS) (Disc 1)`
        - artist: Nightwish  ·  year: 2007
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Singles/2007 Amaranth [Spinefarm spi3111cd Finland]/CD 1`
    - `Amaranth (CDS) (Disc 2)`
        - artist: Nightwish  ·  year: 2007
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Singles/2007 Amaranth [Spinefarm spi3111cd Finland]/CD 2`
    - `Amaranth CD 1`
        - artist: Nightwish  ·  year: 2007
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Singles/2007 Amaranth [Nuclear Blast NB 1925-2 Germany]`
    - `Amaranth CD 2`
        - artist: Nightwish  ·  year: 2007
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Singles/2007 Amaranth [Nuclear Blast NB 1926-2 Germany]`

### Ambient Intermix  (2)
    - `Ambient Intermix CD1`
        - artist: Omicron, Drum Komputer, Human Mesh Dance  ·  year: 1995
        - folder: `D:/music/_cd_rip/Instinct Ambient/[AMB 1002-2] Various - Ambient Intermix`
    - `Ambient Intermix CD2`
        - artist: Facil, Control X, Escape Tank  ·  year: 1995
        - folder: `D:/music/_cd_rip/Instinct Ambient/[AMB 1002-2] Various - Ambient Intermix`

### Ambient Systems  (2)
    - `Ambient Systems CD1`
        - artist: Sub Dub, Adham Shaikh, Terre Thaemlitz  ·  year: 1995
        - folder: `D:/music/_cd_rip/Instinct Ambient/[AMB 002-2] Various - Ambient Systems`
    - `Ambient Systems CD2`
        - artist: Mysteries Of Science, Seti, Deep Space Network  ·  year: 1995
        - folder: `D:/music/_cd_rip/Instinct Ambient/[AMB 002-2] Various - Ambient Systems`

### Ambient Systems II  (2)
    - `Ambient Systems II CD1`
        - artist: Dietrich Schoenemann, Steps-Dis-Charge, Drum Komputer  ·  year: 1996
        - folder: `D:/music/_cd_rip/Instinct Ambient/[AMB 6005-2] Various - Ambient Systems II`
    - `Ambient Systems II CD2`
        - artist: Terre Thaemliz, Futique, Sub Dub  ·  year: 1996
        - folder: `D:/music/_cd_rip/Instinct Ambient/[AMB 6005-2] Various - Ambient Systems II`

### Amplified  (2)
    - `Amplified CD1`
        - artist: Apocalyptica  ·  year: 2006
        - folder: `D:/music/Apocalyptica/2006 Amplified/CD1`
    - `Amplified CD2`
        - artist: Apocalyptica  ·  year: 2006
        - folder: `D:/music/Apocalyptica/2006 Amplified/CD2`

### And What Have You Done With My Body, God?  (3)
    - `And What Have You Done With My Body, God? CD1`
        - artist: The Art of Noise  ·  year: 2006
        - folder: `D:/music/Art of Noise, The - And What Have You Done With My Body, God/CD1 - The Very Start of Noise`
    - `And What Have You Done With My Body, God? CD2`
        - artist: The Art of Noise  ·  year: 2006
        - folder: `D:/music/Art of Noise, The - And What Have You Done With My Body, God/CD2 - Found Sounds & Field Trips`
    - `And What Have You Done With My Body, God? CD4`
        - artist: The Art of Noise  ·  year: 2006
        - folder: `D:/music/Art of Noise, The - And What Have You Done With My Body, God/CD4 - Extended Play`

### Anesthetize  (2)
    - `Anesthetize CD1`
        - artist: Porcupine Tree  ·  year: 2010
        - folder: `D:/music/Porcupine Tree Main discography/Live Albums/2010 - Anesthetize 2CD (Kscope Kscope506 UK)/CD1`
    - `Anesthetize CD2`
        - artist: Porcupine Tree  ·  year: 2010
        - folder: `D:/music/Porcupine Tree Main discography/Live Albums/2010 - Anesthetize 2CD (Kscope Kscope506 UK)/CD2`

### Another Day In Tokyo  (2)
    - `Another Day In Tokyo CD1`
        - artist: Dream Theater  ·  year: 1995
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2016-Another Day In Tokyo`
    - `Another Day In Tokyo CD2`
        - artist: Dream Theater  ·  year: 1995
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2016-Another Day In Tokyo`

### Another Step  (2)
    - `Another Step (CD 1)`
        - artist: Kim Wilde  ·  year: 2010
        - folder: `D:/music/Kim Wilde discography/Kim Wilde 1986 ''Another Step'' (Cherry Pop, 2010, 2CD)/CD 1`
    - `Another Step (CD 2)`
        - artist: Kim Wilde  ·  year: 2010
        - folder: `D:/music/Kim Wilde discography/Kim Wilde 1986 ''Another Step'' (Cherry Pop, 2010, 2CD)/CD 2`

### Anthology  (2)
    - `Anthology (Disc One)`
        - artist: Bryan Adams  ·  year: 2005
        - folder: `D:/music/_cd_rip/Bryan Adams - Discography/2005 - Anthology (2005 - Polydor Limited (UK) - Germany - 987 5798)/CD1`
    - `Anthology (Disc Two)`
        - artist: Bryan Adams  ·  year: 2005
        - folder: `D:/music/_cd_rip/Bryan Adams - Discography/2005 - Anthology (2005 - Polydor Limited (UK) - Germany - 987 5798)/CD2`

### Anthology 2  (2)
    - `Anthology 2 (Disc 1)`
        - artist: The Beatles  ·  year: 1996
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1996 - Anthology 2/Disc 1`
    - `Anthology 2 (Disc 2)`
        - artist: The Beatles  ·  year: 1996
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1996 - Anthology 2/Disc 2`

### Anthology 3  (2)
    - `Anthology 3 (Disc 1)`
        - artist: The Beatles  ·  year: 1996
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1996 - Anthology 3/Disc 1`
    - `Anthology 3 (Disc 2)`
        - artist: The Beatles  ·  year: 1996
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/1996 - Anthology 3/Disc 2`

### Appetite For Destruction  (4)
    - `Appetite For Destruction CD1`
        - artist: Guns N' Roses  ·  year: 1987
        - folder: `D:/music/Guns N' Roses - 1987 - Appetite For Destruction (4CD + Blu-ray Super Deluxe Box Set Universal Music 2018)/CD1`
    - `Appetite For Destruction CD2`
        - artist: Guns N' Roses  ·  year: 2018
        - folder: `D:/music/Guns N' Roses - 1987 - Appetite For Destruction (4CD + Blu-ray Super Deluxe Box Set Universal Music 2018)/CD2`
    - `Appetite For Destruction CD3`
        - artist: Guns N' Roses  ·  year: 2018
        - folder: `D:/music/Guns N' Roses - 1987 - Appetite For Destruction (4CD + Blu-ray Super Deluxe Box Set Universal Music 2018)/CD3`
    - `Appetite For Destruction CD4`
        - artist: Guns N' Roses  ·  year: 2018
        - folder: `D:/music/Guns N' Roses - 1987 - Appetite For Destruction (4CD + Blu-ray Super Deluxe Box Set Universal Music 2018)/CD4`

### Apple Records Extras  (2)
    - `Apple Records Extras - CD1`
        - artist: Badfinger, The Iveys  ·  year: 2010
        - folder: `D:/music/VA - Apple Records Box Set (2010)/CD15 - Various Artists - Apple Records Extras CD1`
    - `Apple Records Extras - CD2`
        - artist: Mary Hopkin, Jackie Lomax, Mary Hopkins  ·  year: 2010
        - folder: `D:/music/VA - Apple Records Box Set (2010)/CD16 - Various Artists - Apple Records Extras CD2`

### At Budokan  (2)
    - `At Budokan (Disc 1)`
        - artist: Bob Dylan  ·  year: 1979
        - folder: `D:/music/_cd_rip/Dylan/Live/1978 - At Budokan/1978. Bob Dylan - At Budokan (Columbia 88691924312.24&25, EU)/Disc1`
    - `At Budokan Disc 2`
        - artist: Bob Dylan  ·  year: 1978
        - folder: `D:/music/_cd_rip/Dylan/Live/1978 - At Budokan/1978. Bob Dylan - At Budokan (Columbia 88691924312.24&25, EU)/Disc2`

### At The Beeb  (2)
    - `At The Beeb -cd 1`
        - artist: Nazareth  ·  year: 1998
        - folder: `D:/music/_cd_rip/Nazareth discography/Live/1998. Nazareth - At The Beeb/1998. Nazareth - At The Beeb (Reef Rec., SRDCD707,England)/disc1`
    - `At The Beeb- cd 2`
        - artist: Nazareth  ·  year: 1998
        - folder: `D:/music/_cd_rip/Nazareth discography/Live/1998. Nazareth - At The Beeb/1998. Nazareth - At The Beeb (Reef Rec., SRDCD707,England)/disc2`

### Avant que l'ombre... а Bercy  (2)
    - `Avant que l'ombre... а Bercy CD1`
        - artist: Mylene Farmer  ·  year: 2006
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2006 - Avant que l'ombre... а Bercy (2CD)/Mylene Farmer - Avant que l'ombre... а Bercy CD1`
    - `Avant que l'ombre... а Bercy CD2`
        - artist: Mylene Farmer  ·  year: 2006
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2006 - Avant que l'ombre... а Bercy (2CD)/Mylene Farmer - Avant que l'ombre... а Bercy CD2`

### B'BOOM: Official Bootleg - Live In Argentina 1994 [DGM9503]  (2)
    - `B'BOOM: Official Bootleg - Live In Argentina 1994 [DGM9503] CD1`
        - artist: King Crimson  ·  year: 1995
        - folder: `D:/music/King Crimson/3.Live/1995. B'BOOM - Official Bootleg/1995, US, Discipline Global Mobile, DGM9503/CD 1`
    - `B'BOOM: Official Bootleg - Live In Argentina 1994 [DGM9503] CD2`
        - artist: King Crimson  ·  year: 1995
        - folder: `D:/music/King Crimson/3.Live/1995. B'BOOM - Official Bootleg/1995, US, Discipline Global Mobile, DGM9503/CD 2`

### BackTracks (Deluxe Collector's Edition) - of 3  (3)
    - `BackTracks (Deluxe Collector's Edition) - Disc 1 of 3`
        - artist: AC/DC  ·  year: 2009
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Box-Sets/2009 - Backtracks (COLUMBIA 88697540982 US)`
    - `BackTracks (Deluxe Collector's Edition) - Disc 2 of 3`
        - artist: AC/DC  ·  year: 2009
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Box-Sets/2009 - Backtracks (COLUMBIA 88697540982 US)`
    - `BackTracks (Deluxe Collector's Edition) - Disc 3 of 3`
        - artist: AC/DC  ·  year: 2009
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Box-Sets/2009 - Backtracks (COLUMBIA 88697540982 US)`

### BBC Live & In-Session (UK, Sanctuary, SMEDD237)  (2)
    - `BBC Live & In-Session (UK, Sanctuary, SMEDD237) (CD1)`
        - artist: Motorhead  ·  year: 2005
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2005-BBC Live & In-Session (2005, UK, Sanctuary, SMEDD237, 2CD)/CD1`
    - `BBC Live & In-Session (UK, Sanctuary, SMEDD237) (CD2)`
        - artist: Motorhead  ·  year: 2005
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2005-BBC Live & In-Session (2005, UK, Sanctuary, SMEDD237, 2CD)/CD2`

### BBC Sessions  (4)
    - `BBC Sessions (CD 1)`
        - artist: Led Zeppelin  ·  year: 1997
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic 7567-83061-2, Germany)/disc1`
    - `BBC Sessions (CD 2)`
        - artist: Led Zeppelin  ·  year: 1997
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic 7567-83061-2, Germany)/disc2`
    - `BBC Sessions - Disc 1`
        - artist: Led Zeppelin  ·  year: 1997
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic AMCY 2401-2, Japan)/disc1`
    - `BBC Sessions [Disk 2]`
        - artist: Led Zeppelin  ·  year: 1969
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/1997. Led Zeppelin - BBC Sessions (Atlantic AMCY 2401-2, Japan)/disc2`

### BBC Sessions 1968-1970 (2011, 2CD, 5099967955121)  (2)
    - `BBC Sessions 1968-1970 CD1 (2011, 2CD, 5099967955121)`
        - artist: Deep Purple  ·  year: 2011
        - folder: `D:/music/_cd_rip/Deep Purple/Compilation Albums/2011. BBC Sessions 1968-1970 (2011, 2CD, EMI, EU, Germany, 5099967955121)/CD1`
    - `BBC Sessions 1968-1970 CD2 (2011, 2CD, 5099967955121)`
        - artist: Deep Purple  ·  year: 2011
        - folder: `D:/music/_cd_rip/Deep Purple/Compilation Albums/2011. BBC Sessions 1968-1970 (2011, 2CD, EMI, EU, Germany, 5099967955121)/CD2`

### Beatles For Sale (MFSL Vinyl Box)  (3)
    - `Beatles For Sale  (MFSL Vinyl Box)`
        - artist: The Beatles  ·  year: 1964
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/04. 1964 - Beatles For Sale {24bit.96kHz Vinyl Rip MFSL 1-104}`
    - `Beatles For Sale (Disc 8)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/08 Beatles For Sale`
    - `Beatles For Sale (No. 2) (Disc 9)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/09 Beatles For Sale (No. 2)`

### Before the Flood  (3)
    - `Before the Flood  (CD 1)`
        - artist: Bob Dylan & The Band  ·  year: 1974
        - folder: `D:/music/_cd_rip/Dylan/Live/1974 - Before The Flood/1974. Bob Dylan - Before The Flood (Columbia CD 22137, Austria)/Disc1`
    - `Before the Flood (CD 2)`
        - artist: Bob Dylan & The Band  ·  year: 1974
        - folder: `D:/music/_cd_rip/Dylan/Live/1974 - Before The Flood/1974. Bob Dylan - Before The Flood (Columbia CD 22137, Austria)/Disc2`
    - `Before the Flood [disc 2]`
        - artist: Bob Dylan & The Band  ·  year: 1974
        - folder: `D:/music/_cd_rip/Dylan/Live/1974 - Before The Flood/1974. Bob Dylan - Before The Flood (Columbia 88691924312.16&17, EU)/Disc2`

### Best 100 - Eternal European Sounds  (5)
    - `Best 100 - Eternal European Sounds (Disc 1)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2014. Best 100 - Eternal European Sounds (5 CD)/Disc 1`
    - `Best 100 - Eternal European Sounds (Disc 2)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2014. Best 100 - Eternal European Sounds (5 CD)/Disc 2`
    - `Best 100 - Eternal European Sounds (Disc 3)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2014. Best 100 - Eternal European Sounds (5 CD)/Disc 3`
    - `Best 100 - Eternal European Sounds (Disc 4)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2014. Best 100 - Eternal European Sounds (5 CD)/Disc 4`
    - `Best 100 - Eternal European Sounds (Disc 5)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2014. Best 100 - Eternal European Sounds (5 CD)/Disc 5`

### Best More - Eternal European Sounds 2  (5)
    - `Best More - Eternal European Sounds 2 (Disc 1)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2016. Best More - Eternal European Sounds 2 (5 CD)/Disc 1`
    - `Best More - Eternal European Sounds 2 (Disc 2)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2016. Best More - Eternal European Sounds 2 (5 CD)/Disc 2`
    - `Best More - Eternal European Sounds 2 (Disc 3)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2016. Best More - Eternal European Sounds 2 (5 CD)/Disc 3`
    - `Best More - Eternal European Sounds 2 (Disc 4)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2016. Best More - Eternal European Sounds 2 (5 CD)/Disc 4`
    - `Best More - Eternal European Sounds 2 (Disc 5)`
        - artist: Paul Mauriat  ·  year: 2014
        - folder: `D:/music/Paul Mauriat (FLAC)/2016. Best More - Eternal European Sounds 2 (5 CD)/Disc 5`

### Better Motorhead Than Dead - Live At Hammersmith (SPV 98172 2CD)  (2)
    - `Better Motorhead Than Dead - Live At Hammersmith (SPV 98172 2CD) (CD1)`
        - artist: Motorhead  ·  year: 2007
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2007-Better Motorhead Than Dead - Live At Hammersmith (2007, Germany, Steamhammer, SPV 98172 2CD, 2CD)/CD1`
    - `Better Motorhead Than Dead - Live At Hammersmith (SPV 98172 2CD) (CD2)`
        - artist: Motorhead  ·  year: 2007
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2007-Better Motorhead Than Dead - Live At Hammersmith (2007, Germany, Steamhammer, SPV 98172 2CD, 2CD)/CD2`

### Billy Elliot  (2)
    - `Billy Elliot CD1`
        - artist: Elton John  ·  year: 2006
        - folder: `D:/music/_cd_rip/Sir Elton John/Soundtracks/2005. Elton John - Billy Elliot The Musical (Polydor 987 537-2, UK)/CD1`
    - `Billy Elliot CD2`
        - artist: Elton John  ·  year: 2006
        - folder: `D:/music/_cd_rip/Sir Elton John/Soundtracks/2005. Elton John - Billy Elliot The Musical (Polydor 987 537-2, UK)/CD2`

### Biograph  (2)
    - `Biograph (Disc 1)`
        - artist: Bob Dylan  ·  year: 1985
        - folder: `D:/music/_cd_rip/Dylan/Compilations/1985. Bob Dylan - Biograph (Columbia 88697 85648 2, EU)/Disc1`
    - `Biograph - Disc 2`
        - artist: Bob Dylan  ·  year: 1985
        - folder: `D:/music/_cd_rip/Dylan/Compilations/1985. Bob Dylan - Biograph (Columbia 88697 85648 2, EU)/Disc2`

### Black Forever/Goodbye America  (2)
    - `Black Forever/Goodbye America CD1`
        - artist: W.A.S.P.  ·  year: 1995
        - folder: `D:/music/W.A.S.P. - Singles & EP/1995 Black Forever CDS ver.1`
    - `Black Forever/Goodbye America CD2`
        - artist: W.A.S.P.  ·  year: 1995
        - folder: `D:/music/W.A.S.P. - Singles & EP/1995 Black Forever CDS ver.2`

### Black Masquerade  (2)
    - `Black Masquerade (CD1)`
        - artist: Rainbow  ·  year: 2013
        - folder: `D:/music/_cd_rip/Rainbow Discography/Live/1995. Rainbow - Black Masquerade/1995. Rainbow - Black Masquerade (Eagle Rec. EDGCD506, Germany)/disc1`
    - `Black Masquerade (CD2)`
        - artist: Rainbow  ·  year: 2013
        - folder: `D:/music/_cd_rip/Rainbow Discography/Live/1995. Rainbow - Black Masquerade/1995. Rainbow - Black Masquerade (Eagle Rec. EDGCD506, Germany)/disc2`

### Black Rain [Tour Edition]  (2)
    - `Black Rain [Tour Edition, CD1]`
        - artist: Ozzy Osbourne  ·  year: 2007
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Studio Albums/2007 Black Rain/2007 Black Rain [2007 USA 88697 20063 2 Sony Tour Edition 2CD]/CD1`
    - `Black Rain [Tour Edition, CD2]`
        - artist: Ozzy Osbourne  ·  year: 2007
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Studio Albums/2007 Black Rain/2007 Black Rain [2007 USA 88697 20063 2 Sony Tour Edition 2CD]/CD2`

### Black Sabbath (Deluxe Expanded Edition)  (2)
    - `Black Sabbath (Deluxe Expanded Edition) CD1`
        - artist: Black Sabbath  ·  year: 1970
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1970 Black Sabbath/1970 Black Sabbath [2009 Germany 2700817 Sanctuary]/CD1`
    - `Black Sabbath (Deluxe Expanded Edition) CD2`
        - artist: Black Sabbath  ·  year: 2009
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1970 Black Sabbath/1970 Black Sabbath [2009 Germany 2700817 Sanctuary]/CD2`

### Black Symphony  (2)
    - `Black Symphony CD1`
        - artist: Within Temptation  ·  year: 2008
        - folder: `D:/music/_cd_rip/Within Temptation - Дискография/Lives/Within Temptation - 2008 - Black Symphony (Live) (2CD)/CD1`
    - `Black Symphony CD2`
        - artist: Within Temptation  ·  year: 2008
        - folder: `D:/music/_cd_rip/Within Temptation - Дискография/Lives/Within Temptation - 2008 - Black Symphony (Live) (2CD)/CD2`

### Blue Moves  (4)
    - `Blue Moves CD1`
        - artist: Elton John  ·  year: 1976
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1976. Blue Moves/1976. Elton John - Blue Moves (Mercury UICY-78966-67, Japan, SHM)/CD1`
    - `Blue Moves CD2`
        - artist: Elton John  ·  year: 1976
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1976. Blue Moves/1976. Elton John - Blue Moves (Mercury UICY-78966-67, Japan, SHM)/CD2`
    - `Blue Moves Disc 1`
        - artist: Elton John k  ·  year: 1976
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1976. Blue Moves/1976. Elton John - Blue Movies (Rocket 532 467-2, Germany)/Disc 1`
    - `Blue Moves Disc 2`
        - artist: Elton John k  ·  year: 1976
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1976. Blue Moves/1976. Elton John - Blue Movies (Rocket 532 467-2, Germany)/Disc 2`

### Bluenote Cafe  (2)
    - `Bluenote Cafe CD1`
        - artist: Young, Neil  ·  year: 1988
        - folder: `D:/music/_cd_rip/Neil Young/Neil Young - Archives Collection/2015. Neil Young - Bluenote Cafe (Reprise 550219-2, USA)/Disc 1`
    - `Bluenote Cafe CD2`
        - artist: Young, Neil  ·  year: 1988
        - folder: `D:/music/_cd_rip/Neil Young/Neil Young - Archives Collection/2015. Neil Young - Bluenote Cafe (Reprise 550219-2, USA)/Disc 2`

### Bob Dylan Bob Dylan Live 1975: The Rolling Thunder Revue  (2)
    - `Bob Dylan   Bob Dylan Live 1975: The Rolling Thunder Revue - Disc 1`
        - artist: Bob Dylan  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2002. Bob Dylan - The Bootleg Series Vol.5 Live 1975 (Columbia-Legacy 88697732902, EU)/Disc1`
    - `Bob Dylan   Bob Dylan Live 1975: The Rolling Thunder Revue - Disc 2`
        - artist: Bob Dylan  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2002. Bob Dylan - The Bootleg Series Vol.5 Live 1975 (Columbia-Legacy 88697732902, EU)/Disc2`

### Bob Dylan Greatest Hits Vol. II  (2)
    - `Bob Dylan Greatest Hits Vol. II (Disc 1)`
        - artist: Bob Dylan  ·  year: 1971
        - folder: `D:/music/_cd_rip/Dylan/Compilations/1971. Bob Dylan - More Bob Dylan Greatest Hits (Columbia 467851 2, Austria)/Disc1`
    - `Bob Dylan Greatest Hits Vol. II (Disc 2)`
        - artist: Bob Dylan  ·  year: 1999
        - folder: `D:/music/_cd_rip/Dylan/Compilations/1971. Bob Dylan - More Bob Dylan Greatest Hits (Columbia 467851 2, Austria)/Disc2`

### Bob Dylan No Direction Home: Bootleg Volume 7 (Movie Soundtrack) \  (2)
    - `Bob Dylan   No Direction Home: Bootleg Volume 7 (Movie Soundtrack) \ CD 1`
        - artist: Bob Dylan  ·  year: 2005
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2005. Bob Dylan - The Bootleg Series Vol.7 No Direction Home (Columbia-legacy 88697732942, EU)/Disc1`
    - `Bob Dylan   No Direction Home: Bootleg Volume 7 (Movie Soundtrack) \ CD 2`
        - artist: Bob Dylan  ·  year: 2005
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2005. Bob Dylan - The Bootleg Series Vol.7 No Direction Home (Columbia-legacy 88697732942, EU)/Disc2`

### Bomber (2005, USA, 06076-86402-2)  (2)
    - `Bomber (2005, USA, 06076-86402-2, CD1)`
        - artist: Motorhead  ·  year: 1979
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1979b-Bomber/1979-Bomber (2005, USA, Sanctuary, 06076-86402-2, 2CD)/CD1`
    - `Bomber (2005, USA, 06076-86402-2, CD2)`
        - artist: Motorhead  ·  year: 1979
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1979b-Bomber/1979-Bomber (2005, USA, Sanctuary, 06076-86402-2, 2CD)/CD2`

### Born Again (Deluxe Expanded Edition- Original Album)  (2)
    - `Born Again (Deluxe Expanded Edition, CD1- Original Album)`
        - artist: Black Sabbath  ·  year: 1983
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1983 Born Again/1983 Born Again [2011 Germany 2770406 Sanctuary]/CD1`
    - `Born Again (Deluxe Expanded Edition, CD2- Bonus Tracks)`
        - artist: Black Sabbath  ·  year: 1983
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1983 Born Again/1983 Born Again [2011 Germany 2770406 Sanctuary]/CD2`

### Born This Way (Japan Special Edition)  (4)
    - `Born This Way (Japan Special Edition) CD 1`
        - artist: Lady GaGa  ·  year: 2011
        - folder: `D:/music/Lady GaGa - Born This Way (Albums and Singles)/Albums/2011 - Born This Way (Japan Special Edition) 2CD/CD 1`
    - `Born This Way (Japan Special Edition) CD 2`
        - artist: Lady GaGa  ·  year: 2011
        - folder: `D:/music/Lady GaGa - Born This Way (Albums and Singles)/Albums/2011 - Born This Way (Japan Special Edition) 2CD/CD 2`
    - `Born This Way (US Special Edition) CD 1`
        - artist: Lady Gaga  ·  year: 2011
        - folder: `D:/music/Lady GaGa - Born This Way (Albums and Singles)/Albums/2011 - Born This Way (US Special Edition) 2CD/CD 1`
    - `Born This Way (US Special Edition) CD 2`
        - artist: Lady Gaga  ·  year: 2011
        - folder: `D:/music/Lady GaGa - Born This Way (Albums and Singles)/Albums/2011 - Born This Way (US Special Edition) 2CD/CD 2`

### Box Set (2001)  (4)
    - `Box Set (2001) - Disc 2`
        - artist: KISS  ·  year: 2001
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2001 Kiss Box Set [USA The Island Def Jam Music Group  314 586 561-2]/CD 2`
    - `Box Set (2001) - Disc 3`
        - artist: KISS  ·  year: 2001
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2001 Kiss Box Set [USA The Island Def Jam Music Group  314 586 561-2]/CD 3`
    - `Box Set (2001) - Disc 4`
        - artist: KISS  ·  year: 2001
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2001 Kiss Box Set [USA The Island Def Jam Music Group  314 586 561-2]/CD 4`
    - `Box Set (2001) - Disc 5`
        - artist: KISS  ·  year: 2001
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2001 Kiss Box Set [USA The Island Def Jam Music Group  314 586 561-2]/CD 5`

### Boxed Set 2  (2)
    - `Boxed Set 2, Disc 1`
        - artist: Led Zeppelin  ·  year: 1993
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1993. Boxed Set 2 (Atlantic 7 82477-2, USA)/Disc 1`
    - `Boxed Set 2, Disc 2`
        - artist: Led Zeppelin  ·  year: 1993
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1993. Boxed Set 2 (Atlantic 7 82477-2, USA)/Disc 2`

### Breaking The Fourth Wall (Live From Boston Opera House)  (3)
    - `Breaking The Fourth Wall (Live From Boston Opera House) CD 1`
        - artist: Dream Theater  ·  year: 2014
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2014-Breaking The Fourth Wall - Live From The Boston Opera House`
    - `Breaking The Fourth Wall (Live From Boston Opera House) CD 2`
        - artist: Dream Theater  ·  year: 2014
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2014-Breaking The Fourth Wall - Live From The Boston Opera House`
    - `Breaking The Fourth Wall (Live From Boston Opera House) CD 3`
        - artist: Dream Theater  ·  year: 2014
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2014-Breaking The Fourth Wall - Live From The Boston Opera House`

### Bridges To Bremen  (2)
    - `Bridges To Bremen (CD1)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Bridges To Bremen [EAGDV108]/CD1`
    - `Bridges To Bremen (CD2)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Bridges To Bremen [EAGDV108]/CD2`

### Bridges To Buenos Aires  (2)
    - `Bridges To Buenos Aires (CD1)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Bridges To Buenos Aires [EAGDV112]/Disc 1`
    - `Bridges To Buenos Aires (CD2)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Bridges To Buenos Aires [EAGDV112]/Disc 2`

### British Steel (2010, 30th Anniversary 2 CD/DVD Deluxe Edition, Sony, 88697667402, Germany)  (2)
    - `British Steel (2010, 30th Anniversary 2 CD/DVD Deluxe Edition, Sony, 88697667402, Germany, CD 1)`
        - artist: Judas Priest  ·  year: 1980
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/1980 - British Steel/2010 - British Steel [Sony, 88697667402, Germany]`
    - `British Steel (2010, 30th Anniversary 2 CD/DVD Deluxe Edition, Sony, 88697667402, Germany, CD 2, Live)`
        - artist: Judas Priest  ·  year: 2010
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/1980 - British Steel/2010 - British Steel [Sony, 88697667402, Germany]`

### Broken, Beat & Scarred  (2)
    - `Broken, Beat & Scarred (Disc 1)`
        - artist: Metallica  ·  year: 2009
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/2009 - Broken, Beat & Scarred/2009 EU 3 CD Set/Disc 1, Vertigo 2702224`
    - `Broken, Beat & Scarred (Disc 2)`
        - artist: Metallica  ·  year: 2009
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/2009 - Broken, Beat & Scarred/2009 EU 3 CD Set/Disc 2, Vertigo 2702228`

### Brussels Affair (Live 1973)  (2)
    - `Brussels Affair (Live 1973) CD1`
        - artist: The Rolling Stones  ·  year: 2015
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2015 The Marquee Club Live In 1971 [Ward  GQBS 90009-12]/2-3 The Brussels Affair (Live 1973)/Disc 1`
    - `Brussels Affair (Live 1973) CD2`
        - artist: The Rolling Stones  ·  year: 2015
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2015 The Marquee Club Live In 1971 [Ward  GQBS 90009-12]/2-3 The Brussels Affair (Live 1973)/Disc 2`

### Ca Ira  (2)
    - `Ca Ira - CD1`
        - artist: Roger Waters  ·  year: 2005
        - folder: `D:/music/Pink Floyd - Solo Albums (lossless)/Roger Waters/2005 - Ca Ira/Disc-1`
    - `Ca Ira - CD2`
        - artist: Roger Waters  ·  year: 2005
        - folder: `D:/music/Pink Floyd - Solo Albums (lossless)/Roger Waters/2005 - Ca Ira/Disc-2`

### Captain Fantastic and the Brown Dirt Cowboy Deluxe Edition  (2)
    - `Captain Fantastic and the Brown Dirt Cowboy Deluxe Edition Disc 1`
        - artist: Elton John  ·  year: 1975
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1975. Captain Fantastic And The Brown Dirt Cowboy/1975. Elton John - Captain Fantastic and the Brown Dirt Cowboy (Island B0005357-02, USA)/CD1`
    - `Captain Fantastic and the Brown Dirt Cowboy Deluxe Edition Disc 2`
        - artist: Elton John  ·  year: 1975
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1975. Captain Fantastic And The Brown Dirt Cowboy/1975. Elton John - Captain Fantastic and the Brown Dirt Cowboy (Island B0005357-02, USA)/CD2`

### Carnets de scene (live)  (2)
    - `Carnets de scene (live) - CD 1`
        - artist: Patricia Kaas  ·  year: 1991
        - folder: `D:/music/Patrisia Kaas/Carnets De Scene/CD1`
    - `Carnets de scene (live) - CD 2`
        - artist: Patricia Kaas  ·  year: 1991
        - folder: `D:/music/Patrisia Kaas/Carnets De Scene/CD2`

### Carnival of Sins Live  (2)
    - `Carnival of Sins Live - CD1`
        - artist: Motley Crue  ·  year: 2006
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2006 - Carnival Of Sins Live [Motley Rec., SPV 97512 2CD-E, Germany]`
    - `Carnival of Sins Live - CD2`
        - artist: Motley Crue  ·  year: 2006
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2006 - Carnival Of Sins Live [Motley Rec., SPV 97512 2CD-E, Germany]`

### Celebration (Deluxe Edition)  (2)
    - `Celebration (Deluxe Edition) Disc 1`
        - artist: Madonna  ·  year: 2009
        - folder: `D:/music/_cd_rip/Madonna - Discography/2009 - Celebration (Deluxe Edition) (2009 - Warner Bros. Records Inc. - Germany - 9362-49729-6)/Disc One`
    - `Celebration (Deluxe Edition) Disc 2`
        - artist: Madonna  ·  year: 2009
        - folder: `D:/music/_cd_rip/Madonna - Discography/2009 - Celebration (Deluxe Edition) (2009 - Warner Bros. Records Inc. - Germany - 9362-49729-6)/Disc Two`

### Celebration Day  (2)
    - `Celebration Day (Disc 2)`
        - artist: Led Zeppelin  ·  year: 2007
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2012. Celebration Day/2012. Led Zeppelin - Celebration Day (Atlantic- Swan Song 8122-79688-7, EU)/disc2`
    - `Celebration Day - Disc 1`
        - artist: Led Zeppelin  ·  year: 2012
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2012. Celebration Day/2012. Led Zeppelin - Celebration Day (Atlantic- Swan Song 8122-79688-7, EU)/disc1`

### Celebrator  (2)
    - `Celebrator CD1`
        - artist: U.D.O., HAMMERFALL feat. Udo Dirkschneider, LORDI feat. Udo Dirkschneider  ·  year: 2012
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Compilations/2012 Celebrator - Rare Tracks/2012 Celebrator - Rare Tracks [2012 AFM 407-9 Germany]/CD 1`
    - `Celebrator CD2`
        - artist: U.D.O., U.D.O. feat. Faktor2, U.D.O. feat. Doro  ·  year: 2012
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Compilations/2012 Celebrator - Rare Tracks/2012 Celebrator - Rare Tracks [2012 AFM 407-9 Germany]/CD 2`

### Chaos In Motion  (3)
    - `Chaos In Motion CD 1`
        - artist: Dream Theater  ·  year: 2008
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2008-Chaos In Motion`
    - `Chaos In Motion CD 2`
        - artist: Dream Theater  ·  year: 2008
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2008-Chaos In Motion`
    - `Chaos In Motion CD 3`
        - artist: Dream Theater  ·  year: 2008
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2008-Chaos In Motion`

### Collection (ORANGE236)  (2)
    - `Collection (Disc 1) (ORANGE236)`
        - artist: Rod Stewart  ·  year: 2008
        - folder: `D:/music/_cd_rip/ROD STEWART/Rod Stewart - 2008 - Collection 2CD (Weton-Wesgram ORANGE236 UK)/CD1`
    - `Collection (Disc 2) (ORANGE236)`
        - artist: Rod Stewart  ·  year: 2008
        - folder: `D:/music/_cd_rip/ROD STEWART/Rod Stewart - 2008 - Collection 2CD (Weton-Wesgram ORANGE236 UK)/CD2`

### Coma Divine - Recorded Live In Rome  (2)
    - `Coma Divine - Recorded Live In Rome CD1`
        - artist: Porcupine Tree  ·  year: 1997
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1997 - Coma Divine 2CD (Expanded Edition 2002) (Kscope KSCOPE130 China)/CD1`
    - `Coma Divine - Recorded Live in Rome CD2`
        - artist: Porcupine Tree  ·  year: 1997
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1997 - Coma Divine 2CD (Expanded Edition 2002) (Kscope KSCOPE130 China)/CD2`

### Coma Divine - Recorded Live in Rome - Japan (IECP-20120)  (2)
    - `Coma Divine - Recorded Live in Rome (Cd 1) - Japan (IECP-20120)`
        - artist: Porcupine Tree  ·  year: 1997
        - folder: `D:/music/Porcupine Tree Main discography/Japan Releases/1997 - Coma Divine. Live In Rome 2CD (WHD Entertainment, Inc. IECP-20120-121 Japan)/CD1`
    - `Coma Divine - Recorded Live in Rome (Cd 2) - Japan (IECP-20121)`
        - artist: Porcupine Tree  ·  year: 1997
        - folder: `D:/music/Porcupine Tree Main discography/Japan Releases/1997 - Coma Divine. Live In Rome 2CD (WHD Entertainment, Inc. IECP-20120-121 Japan)/CD2`

### Come Taste The Band. 35th Anniversary (2010, 2CD, 5099964786629)  (2)
    - `Come Taste The Band. 35th Anniversary CD1 (2010, 2CD, 5099964786629)`
        - artist: Deep Purple  ·  year: 1975
        - folder: `D:/music/_cd_rip/Deep Purple/Studio Albums/1975. Come Taste The Band/1975. Come Taste The Band. 35th Anniversary (2010, 2CD, EMI, EU, Poland, 5099964786629)/CD1`
    - `Come Taste The Band. 35th Anniversary CD2 (2010, 2CD, 5099964786629)`
        - artist: Deep Purple  ·  year: 2010
        - folder: `D:/music/_cd_rip/Deep Purple/Studio Albums/1975. Come Taste The Band/1975. Come Taste The Band. 35th Anniversary (2010, 2CD, EMI, EU, Poland, 5099964786629)/CD2`

### Concert For Bangladesh (2CDs)  (2)
    - `Concert For Bangladesh (2CDs) (CD1)`
        - artist: George Harrison, George Harrison, Ravi Shankar, Ravi Shankar  ·  year: 1971
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/02 Live Albums/1971 Concert For Bagla Desh (Epic ESCA 5470~1)/Disc 1`
    - `Concert For Bangladesh (2CDs) (CD2)`
        - artist: Bob Dylan, George Harrison, Leon Russell  ·  year: 1971
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/02 Live Albums/1971 Concert For Bagla Desh (Epic ESCA 5470~1)/Disc 2`

### Concerto For Group And Orchestra  (4)
    - `Concerto For Group And Orchestra - Disc 1`
        - artist: Deep Purple  ·  year: 2002
        - folder: `D:/music/DEEP PURPLE - Concerto For Group And Orchestra (1969, 2002) [SACD] (ISO)/Disc1`
    - `Concerto For Group And Orchestra - Disc 2`
        - artist: Deep Purple  ·  year: 2002
        - folder: `D:/music/DEEP PURPLE - Concerto For Group And Orchestra (1969, 2002) [SACD] (ISO)/Disc2`
    - `Concerto for Group and Orchestra CD1 (2002, 2CD, 0724354100628)`
        - artist: Deep Purple  ·  year: 1969
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1969. Concerto for Group and Orchestra (2002, 2CD, Harvest, EU, 0724354100628)/CD1`
    - `Concerto for Group and Orchestra CD2 (2002, 2CD, 0724354100628)`
        - artist: Deep Purple  ·  year: 1969
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1969. Concerto for Group and Orchestra (2002, 2CD, Harvest, EU, 0724354100628)/CD2`

### Copenhagen 1972 (2013, 2CD, 0208369ERE)  (2)
    - `Copenhagen 1972 CD1 (2013, 2CD, 0208369ERE)`
        - artist: Deep Purple  ·  year: 2005
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2002. Copenhagen 1972 (2013, 2CD, earMUSIC, Germany, 0208369ERE)/CD1`
    - `Copenhagen 1972 CD2 (2013, 2CD, 0208369ERE)`
        - artist: Deep Purple  ·  year: 2005
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2002. Copenhagen 1972 (2013, 2CD, earMUSIC, Germany, 0208369ERE)/CD2`

### Cre-Ation  (2)
    - `Cre-Ation  CD1`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/Compilation/2016 Cre-ation - The Early Years 1967-1972/(PFR PFREY8) [EU 2016 issue]/CD1`
    - `Cre-Ation CD2`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/Compilation/2016 Cre-ation - The Early Years 1967-1972/(PFR PFREY8) [EU 2016 issue]/CD2`

### Crystal Ball  (4)
    - `Crystal Ball CD1`
        - artist: Prince  ·  year: 1997
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1997 - Crystal Ball (3CD set + 2 bonus discs - 'The Truth', 'Kamasutra')/CD1`
    - `Crystal Ball CD2`
        - artist: Prince  ·  year: 1997
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1997 - Crystal Ball (3CD set + 2 bonus discs - 'The Truth', 'Kamasutra')/CD2`
    - `Crystal Ball CD3`
        - artist: Prince  ·  year: 1997
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1997 - Crystal Ball (3CD set + 2 bonus discs - 'The Truth', 'Kamasutra')/CD3`
    - `Crystal Ball CD5 (Kamasutra)`
        - artist: Prince  ·  year: 1997
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1997 - Crystal Ball (3CD set + 2 bonus discs - 'The Truth', 'Kamasutra')/CD5 - Kamasutra (bonus disc)`

### Cult - Special Edition  (2)
    - `Cult - Special Edition (Disc I)`
        - artist: Apocalyptica  ·  year: 2001
        - folder: `D:/music/Apocalyptica/2001 Cult (Special Edition)/CD1`
    - `Cult - Special Edition (Disc II)`
        - artist: Apocalyptica  ·  year: 2001
        - folder: `D:/music/Apocalyptica/2001 Cult (Special Edition)/CD2`

### Dance Classics 'Pop Edition' vol.9  (2)
    - `Dance Classics 'Pop Edition' vol.9 [CD1]`
        - artist: VA  ·  year: 2012
        - folder: `D:/music/_cd_rip/VA - Dance Classics - Pop Edition/2012 VA - Dance Classics - Pop Edition Vol. 9 [RDM291]/CD1`
    - `Dance Classics 'Pop Edition' vol.9 [CD2]`
        - artist: VA  ·  year: 2012
        - folder: `D:/music/_cd_rip/VA - Dance Classics - Pop Edition/2012 VA - Dance Classics - Pop Edition Vol. 9 [RDM291]/CD2`

### Dance Classics Pop Edition vol.7  (2)
    - `Dance Classics Pop Edition  vol.7 (CD1)`
        - artist: VA  ·  year: 2011
        - folder: `D:/music/_cd_rip/VA - Dance Classics - Pop Edition/2011 VA - Dance Classics - Pop Edition Vol. 7 [RDM249]/CD1`
    - `Dance Classics Pop Edition vol.7 (CD2)`
        - artist: VA  ·  year: 2012
        - folder: `D:/music/_cd_rip/VA - Dance Classics - Pop Edition/2011 VA - Dance Classics - Pop Edition Vol. 7 [RDM249]/CD2`

### Dance Classics Pop Edition vol.8  (2)
    - `Dance Classics Pop Edition vol.8 (CD1)`
        - artist: VA  ·  year: 2012
        - folder: `D:/music/_cd_rip/VA - Dance Classics - Pop Edition/2012 VA - Dance Classics - Pop Edition Vol. 8 [RDM263]/CD1`
    - `Dance Classics Pop Edition vol.8 (CD2)`
        - artist: VA  ·  year: 2012
        - folder: `D:/music/_cd_rip/VA - Dance Classics - Pop Edition/2012 VA - Dance Classics - Pop Edition Vol. 8 [RDM263]/CD2`

### Dance Remixes  (2)
    - `Dance Remixes CD1`
        - artist: Mylene Farmer  ·  year: 1992
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/1992 - Dance Remixes (2CD)/Mylene Farmer - Dance Remixes CD1`
    - `Dance Remixes CD2`
        - artist: Mylene Farmer  ·  year: 1992
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/1992 - Dance Remixes (2CD)/Mylene Farmer - Dance Remixes CD2`

### Dark Passion Play (Collector's Edition)  (2)
    - `Dark Passion Play (Collector's Edition) Disc 1`
        - artist: Nightwish  ·  year: 2007
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Studio Albums/2007 Dark Passion Play/2007 Dark Passion Play [2007 Roadrunner 1686-179702 USA]/CD 1`
    - `Dark Passion Play (Collector's Edition) Disc 2`
        - artist: Nightwish  ·  year: 2007
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Studio Albums/2007 Dark Passion Play/2007 Dark Passion Play [2007 Roadrunner 1686-179702 USA]/CD 2`

### Deadly Sting: The Mercury Years (314 534 344-2)  (2)
    - `Deadly Sting: The Mercury Years (314 534 344-2) (Disc 1)`
        - artist: Scorpions  ·  year: 1997
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Compilations/1997 Deadly Sting - The Mercury Years/1997 Deadly Sting - The Mercury Years [1997 USA 314 534 344-2 PolyGram]/CD 1`
    - `Deadly Sting: The Mercury Years (314 534 344-2) (Disc 2)`
        - artist: Scorpions  ·  year: 1997
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Compilations/1997 Deadly Sting - The Mercury Years/1997 Deadly Sting - The Mercury Years [1997 USA 314 534 344-2 PolyGram]/CD 2`

### Decade  (2)
    - `Decade (Disc 1)`
        - artist: Neil Young  ·  year: 1977
        - folder: `D:/music/_cd_rip/Neil Young/1977. Neil Young - Decade (2 CD) (2002 Reprise Records, 7599-27233-2, Germany)/Disc One`
    - `Decade [Disc 2]`
        - artist: Neil Young  ·  year: 1977
        - folder: `D:/music/_cd_rip/Neil Young/1977. Neil Young - Decade (2 CD) (2002 Reprise Records, 7599-27233-2, Germany)/Disc Two`

### Defenders Of The Faith (2015, Sony, 30th Anniversary 3 CD Deluxe Edition, Remastered, SICP 4388, Japan)  (3)
    - `Defenders Of The Faith (2015, Sony, 30th Anniversary 3 CD Deluxe Edition, CD 1, Remastered, SICP 4388, Japan)`
        - artist: Judas Priest  ·  year: 1984
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/1984 - Defenders Of The Faith/2015 - Defenders Of The Faith [Sony, SICP 4388~90, Japan]`
    - `Defenders Of The Faith (2015, Sony, 30th Anniversary 3 CD Deluxe Edition, CD 2, Live, SICP 4389, Japan)`
        - artist: Judas Priest  ·  year: 2015
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/1984 - Defenders Of The Faith/2015 - Defenders Of The Faith [Sony, SICP 4388~90, Japan]`
    - `Defenders Of The Faith (2015, Sony, 30th Anniversary 3 CD Deluxe Edition, CD 3, Live, SICP 4390, Japan)`
        - artist: Judas Priest  ·  year: 2015
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/1984 - Defenders Of The Faith/2015 - Defenders Of The Faith [Sony, SICP 4388~90, Japan]`

### Dehumanizer  (2)
    - `Dehumanizer (CD1)`
        - artist: Black Sabbath  ·  year: 2011
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1992 Dehumanizer/1992 Dehumanizer [2011 EU EIRSCDX 1064 EMI]/CD1`
    - `Dehumanizer (CD2)`
        - artist: Black Sabbath  ·  year: 2011
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1992 Dehumanizer/1992 Dehumanizer [2011 EU EIRSCDX 1064 EMI]/CD2`

### Delicate Sound Of Thunder  (6)
    - `Delicate Sound Of Thunder (CD1)`
        - artist: Pink Floyd  ·  year: 1988
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(CBS 463161 2) [1st Australia]/CD1`
    - `Delicate Sound Of Thunder (CD2)`
        - artist: Pink Floyd  ·  year: 1988
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(CBS 463161 2) [1st Australia]/CD2`
    - `Delicate Sound Of Thunder - Disc 2`
        - artist: Pink Floyd  ·  year: 1988
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(Columbia C2K 44484) [4th USA]/CD2`
    - `Delicate Sound of Thunder (Disc 1)`
        - artist: Pink Floyd  ·  year: 1988
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(Columbia C2K 44484) [1st USA]/CD1`
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(Columbia C2K 44484) [2nd USA]/CD1`
    - `Delicate Sound of Thunder (Disc 2)`
        - artist: Pink Floyd  ·  year: 1988
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(Columbia C2K 44484) [1st USA]/CD2`
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(Columbia C2K 44484) [2nd USA]/CD2`
    - `Delicate Sound of Thunder (Disc 2) (MHCP 687)`
        - artist: Pink Floyd  ·  year: 1988
        - folder: `D:/music/Pink Floyd/Live/1988 Delicate Sound Of Thunder/(Sony MHCP 686-87) [4th Japan]/Disc 2`

### Demolition (2001, Atlantic, 7567930922, Australia, 2 CD Tour Edition)  (2)
    - `Demolition (2001, Atlantic, 7567930922, Australia, 2 CD Tour Edition, CD 1)`
        - artist: Judas Priest  ·  year: 2001
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/2001 - Demolition/2001 - Demolition [Atlantic, 7567930922, Australia]`
    - `Demolition (2001, Atlantic, 7567930922, Australia, 2 CD Tour Edition, CD 2)`
        - artist: Judas Priest  ·  year: 2001
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/2001 - Demolition/2001 - Demolition [Atlantic, 7567930922, Australia]`

### Diamonds  (2)
    - `Diamonds (CD1)`
        - artist: Elton John  ·  year: 2017
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/2017. Elton John - Diamonds (Virgin-UMC 6700657, EU)/Disc 1`
    - `Diamonds -  CD2`
        - artist: Elton John  ·  year: 2017
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/2017. Elton John - Diamonds (Virgin-UMC 6700657, EU)/Disc 2`

### Dio's Inferno - The Last In Live  (2)
    - `Dio's Inferno - The Last In Live (CD 1)`
        - artist: Dio  ·  year: 1998
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/1997 Dio's Inferno - The Last In Live/1997 Dio's Inferno - The Last In Live [1998 SPV 085-18842 DCD Germany]/CD 1`
    - `Dio's Inferno - The Last In Live (CD 2)`
        - artist: Dio  ·  year: 1998
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/1997 Dio's Inferno - The Last In Live/1997 Dio's Inferno - The Last In Live [1998 SPV 085-18842 DCD Germany]/CD 2`

### Double Live Assassins  (8)
    - `Double Live Assassins (Disc 1)`
        - artist: W.A.S.P.  ·  year: 1998
        - folder: `D:/music/W.A.S.P. - 1998 - Double Live Assassins (2CD)/CD1`
    - `Double Live Assassins (Disc 2)`
        - artist: W.A.S.P.  ·  year: 1998
        - folder: `D:/music/W.A.S.P. - 1998 - Double Live Assassins (2CD)/CD2`
    - `Double Live Assassins CD1 [Madfish, SMACD986, Poland]`
        - artist: W.A.S.P  ·  year: 2012
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/1998 - Double Live Assassins 2CD [2012, Madfish, SMACD986, Poland]`
    - `Double Live Assassins CD1 [Sanctuary~Snapper, SAP CD 901, UK]`
        - artist: W.A.S.P.  ·  year: 1998
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/1998 - Double Live Assassins 2CD [1998, Sanctuay~Snapper, SAP CD 901, UK]`
    - `Double Live Assassins CD2 [Madfish, SMACD986, Poland]`
        - artist: W.A.S.P  ·  year: 2012
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/1998 - Double Live Assassins 2CD [2012, Madfish, SMACD986, Poland]`
    - `Double Live Assassins CD2 [Sanctuary~Snapper, SAP CD 901, UK]`
        - artist: W.A.S.P.  ·  year: 1998
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/1998 - Double Live Assassins 2CD [1998, Sanctuay~Snapper, SAP CD 901, UK]`
    - `Double Live Assassins [CD1] [VICP-60209]`
        - artist: W.A.S.P.  ·  year: 1998
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/1998 - Double Live Assassins 2CD [1998, Victor, VICP-60209~10, Japan]`
    - `Double Live Assassins [CD2] [VICP-60210]`
        - artist: W.A.S.P.  ·  year: 1998
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/1998 - Double Live Assassins 2CD [1998, Victor, VICP-60209~10, Japan]`

### Dr. Feelgood [Universal, UICY-94254~5, Japan]  (2)
    - `Dr. Feelgood CD1 [Universal, UICY-94254~5, Japan]`
        - artist: Motley Crue  ·  year: 2009
        - folder: `D:/music/_cd_rip/Motley Crue/1989 - Dr. Feelgood/2009 - Dr. Feelgood 2CD [Universal, SHM-CD, UICY-94254~5, Japan]`
    - `Dr. Feelgood CD2 [Universal, UICY-94254~5, Japan]`
        - artist: Motley Crue  ·  year: 2009
        - folder: `D:/music/_cd_rip/Motley Crue/1989 - Dr. Feelgood/2009 - Dr. Feelgood 2CD [Universal, SHM-CD, UICY-94254~5, Japan]`

### Dream Evil (2013, Deluxe Expanded Edition)  (2)
    - `Dream Evil (2013, Deluxe Expanded Edition, Disc 1)`
        - artist: Dio  ·  year: 1987
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1987 Dream Evil/1987 Dream Evil [2013 Universal 3727788 Germany]/Dics 1 - Dream Evil`
    - `Dream Evil (2013, Deluxe Expanded Edition, Disc 2)`
        - artist: Dio  ·  year: 2013
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1987 Dream Evil/1987 Dream Evil [2013 Universal 3727788 Germany]/Disc 2 - Bonus Tracks`

### Dying To Live Forever  (2)
    - `Dying To Live Forever CD 1`
        - artist: Dream Theater  ·  year: 2015
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2015-Dying To Live Forever`
    - `Dying To Live Forever CD 2`
        - artist: Dream Theater  ·  year: 2015
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2015-Dying To Live Forever`

### Ein Sound Erobert Die Welt  (5)
    - `Ein Sound Erobert Die Welt (CD1)`
        - artist: James Last & His Band  ·  year: 1992
        - folder: `D:/music/JAMES LAST COLLECTION/1992 - Ein Sound Erobert Die Welt (Reader's Digest JLS 7229) 5CD`
    - `Ein Sound Erobert Die Welt (CD2)`
        - artist: James Last & His Band  ·  year: 1992
        - folder: `D:/music/JAMES LAST COLLECTION/1992 - Ein Sound Erobert Die Welt (Reader's Digest JLS 7229) 5CD`
    - `Ein Sound Erobert Die Welt (CD3)`
        - artist: James Last & His Band  ·  year: 1992
        - folder: `D:/music/JAMES LAST COLLECTION/1992 - Ein Sound Erobert Die Welt (Reader's Digest JLS 7229) 5CD`
    - `Ein Sound Erobert Die Welt (CD4)`
        - artist: James Last & His Band  ·  year: 1992
        - folder: `D:/music/JAMES LAST COLLECTION/1992 - Ein Sound Erobert Die Welt (Reader's Digest JLS 7229) 5CD`
    - `Ein Sound Erobert Die Welt (CD5)`
        - artist: James Last & His Band  ·  year: 1992
        - folder: `D:/music/JAMES LAST COLLECTION/1992 - Ein Sound Erobert Die Welt (Reader's Digest JLS 7229) 5CD`

### Elton John (5 Classic Albums, Box Set)  (3)
    - `Elton John (5 Classic Albums, Box Set) CD1`
        - artist: Elton John  ·  year: 1970
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2012. Elton John - 5 Classic Albums (1970-1973) (Mercury 00602537069118, EU)/CD1. 1970. Elton John`
    - `Elton John CD1`
        - artist: Elton John  ·  year: 1969
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1970. Elton John/1970. Elton John - Elton John (Mercury 06007 5305559 5, EC)/Disc 1`
    - `Elton John CD2`
        - artist: Elton John  ·  year: 1970
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1970. Elton John/1970. Elton John - Elton John (Mercury 06007 5305559 5, EC)/Disc 2`

### Emancipation  (3)
    - `Emancipation CD1`
        - artist: Prince  ·  year: 1996
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1996 - Emancipation (3CD set)/CD1`
    - `Emancipation CD2`
        - artist: Prince, Unknown Artist, Friend, Lover, Sister, Mother/Wife  ·  year: 1996
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1996 - Emancipation (3CD set)/CD2`
    - `Emancipation CD3`
        - artist: Prince  ·  year: 1996
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1996 - Emancipation (3CD set)/CD3`

### En concert  (2)
    - `En concert CD1`
        - artist: Mylene Farmer  ·  year: 1989
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/1989 - En concert (2CD)/Mylene Farmer - En concert CD1`
    - `En concert CD2`
        - artist: Mylene Farmer  ·  year: 1989
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/1989 - En concert (2CD)/Mylene Farmer - En concert CD2`

### End Of An Era  (2)
    - `End Of An Era CD 1`
        - artist: Nightwish  ·  year: 2006
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Live Albums/2006 End Of An Era/2006 End Of An Era [2006 Nuclear Blast NB 1679-0 Germany]/CD 1`
    - `End Of An Era CD 2`
        - artist: Nightwish  ·  year: 2006
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Live Albums/2006 End Of An Era/2006 End Of An Era [2006 Nuclear Blast NB 1679-0 Germany]/CD 2`

### Endless Forms Most Beautiful  (3)
    - `Endless Forms Most Beautiful Disc 1`
        - artist: Nightwish  ·  year: 2015
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Studio Albums/2015 Endless Forms Most Beautiful/2015 Endless Forms Most Beautiful [2015 Nuclear Blast NB 3464-5 Germany]/CD 1`
    - `Endless Forms Most Beautiful Disc 2`
        - artist: Nightwish  ·  year: 2015
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Studio Albums/2015 Endless Forms Most Beautiful/2015 Endless Forms Most Beautiful [2015 Nuclear Blast NB 3464-5 Germany]/CD 2`
    - `Endless Forms Most Beautiful Disc 3`
        - artist: Nightwish  ·  year: 2015
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Studio Albums/2015 Endless Forms Most Beautiful/2015 Endless Forms Most Beautiful [2015 Nuclear Blast NB 3464-5 Germany]/CD 3`

### Essential  (2)
    - `Essential Disc 1`
        - artist: Bob Dylan  ·  year: 2000
        - folder: `D:/music/_cd_rip/Dylan/Compilations/2000. Bob Dylan - The Essential (Columbia C2K 85168, USA)/Disc1`
    - `Essential Disc 2`
        - artist: Bob Dylan  ·  year: 2000
        - folder: `D:/music/_cd_rip/Dylan/Compilations/2000. Bob Dylan - The Essential (Columbia C2K 85168, USA)/Disc2`

### Everything Louder Than Everyone Else (SPV 089-21140 DCD)  (2)
    - `Everything Louder Than Everyone Else (SPV 089-21140 DCD) (CD1)`
        - artist: Motorhead  ·  year: 1999
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/1999-Everything Louder Than Everyone Else (1999, Germany, Steamhammer, SPV 089-21140 DCD, 2CD)/CD1`
    - `Everything Louder Than Everyone Else (SPV 089-21140 DCD) (CD2)`
        - artist: Motorhead  ·  year: 1999
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/1999-Everything Louder Than Everyone Else (1999, Germany, Steamhammer, SPV 089-21140 DCD, 2CD)/CD2`

### Exile On Main St (B0014130-02)  (2)
    - `Exile On Main St (Disc 1) (B0014130-02)`
        - artist: The Rolling Stones  ·  year: 2010
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/14. 1972 Exile on Main St/Exile On Main St [RSR B0014130-02]/Disc 1`
    - `Exile On Main St (Disc 2) (B0014130-02)`
        - artist: The Rolling Stones  ·  year: 2010
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/14. 1972 Exile on Main St/Exile On Main St [RSR B0014130-02]/Disc 2`

### Falling Into Infinity [Japan]  (2)
    - `Falling Into Infinity [Japan] CD 1`
        - artist: Dream Theater  ·  year: 1997
        - folder: `D:/music/_cd_rip/Dream Theater/Albums/1997-Falling Into Infinity/1997-EastWest Japan [Japan, AMCY-2315]`
    - `Falling Into Infinity [Japan] CD 2`
        - artist: Dream Theater  ·  year: 1997
        - folder: `D:/music/_cd_rip/Dream Theater/Albums/1997-Falling Into Infinity/1997-EastWest Japan [Japan, AMCY-2315]`

### Fear Of The Dark (7243 8 35877 2 8)  (4)
    - `Fear Of The Dark (7243 8 35877 2 8, CD1)`
        - artist: Iron Maiden  ·  year: 1992
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1992 Fear Of The Dark/1992 Fear Of The Dark [1995 Holland 7243 8 35877 2 8 EMI]/CD 1`
    - `Fear Of The Dark (7243 8 35877 2 8, CD2)`
        - artist: Iron Maiden  ·  year: 1992
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1992 Fear Of The Dark/1992 Fear Of The Dark [1995 Holland 7243 8 35877 2 8 EMI]/CD 2`
    - `Fear Of The Dark [Castle 111-2] CD 1`
        - artist: Iron Maiden  ·  year: 1992
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1992 Fear Of The Dark/1992 Fear Of The Dark [1995 USA CASTLE 111-2. Castle Records]/CD 1`
    - `Fear Of The Dark [Castle 111-2] CD 2`
        - artist: Iron Maiden  ·  year: 1992
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1992 Fear Of The Dark/1992 Fear Of The Dark [1995 USA CASTLE 111-2. Castle Records]/CD 2`

### Finding The Sacred Heart - Live In Philly 1986  (2)
    - `Finding The Sacred Heart - Live In Philly 1986 (CD1)`
        - artist: Dio  ·  year: 2013
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/2013 Finding The Sacred Heart – Live In Philly 1986/2013 Finding The Sacred Heart – Live In Philly 1986 [2013 Niji EDGCD498 Germany]/CD 1`
    - `Finding The Sacred Heart - Live In Philly 1986 (CD2)`
        - artist: Dio  ·  year: 2013
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/2013 Finding The Sacred Heart – Live In Philly 1986/2013 Finding The Sacred Heart – Live In Philly 1986 [2013 Niji EDGCD498 Germany]/CD 2`

### Finyl Vinyl  (4)
    - `Finyl Vinyl (Disc 1)`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/1986. Rainbow - Finyl Vinyl/1986. Rainbow - Fynil Vinyl (Polydor 547 368-2, Germany)/Disc1`
    - `Finyl Vinyl (Disc Two)`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow Discography/Compilations/1986. Rainbow - Finyl Vinyl/1986. Rainbow - Fynil Vinyl (Polydor 547 368-2, Germany)/Disc2`
    - `Finyl Vinyl (SHM-CD Japanese UICY-93626) Disc 1`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow-(SHM-CD_Japanese_UICY-93618-27)/(SHM-CD_Japanese_UICY-93626-7)-Finyl_Vinyl/CD1`
    - `Finyl Vinyl (SHM-CD Japanese UICY-93627) Disc 2`
        - artist: Rainbow  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rainbow-(SHM-CD_Japanese_UICY-93618-27)/(SHM-CD_Japanese_UICY-93626-7)-Finyl_Vinyl/CD2`

### Forbidden Dreams - Festival Hall Osaka, Japan 1993-08-28  (2)
    - `Forbidden Dreams CD 3 - Festival Hall Osaka, Japan 1993-08-28`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2002-Forbidden Dreams`
    - `Forbidden Dreams CD 4 - Festival Hall Osaka, Japan 1993-08-28`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2002-Forbidden Dreams`

### Forbidden Dreams - Imperial Hall Osaka, Japan 1992-11-19  (2)
    - `Forbidden Dreams CD 1 - Imperial Hall Osaka, Japan 1992-11-19`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2002-Forbidden Dreams`
    - `Forbidden Dreams CD 2 - Imperial Hall Osaka, Japan 1992-11-19`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2002-Forbidden Dreams`

### Forbidden Dreams - Shinjuku Koseinenkin Kaikan Hall Tokyo, Japan 1995-01-23  (2)
    - `Forbidden Dreams CD 5 - Shinjuku Koseinenkin Kaikan Hall Tokyo, Japan 1995-01-23`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2002-Forbidden Dreams`
    - `Forbidden Dreams CD 6 - Shinjuku Koseinenkin Kaikan Hall Tokyo, Japan 1995-01-23`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2002-Forbidden Dreams`

### Forever Best!  (2)
    - `Forever Best! CD1`
        - artist: Sade  ·  year: 2005
        - folder: `D:/music/_cd_rip/Sade (Japan Originals & Remasters) [torrents.ru]/05_Bootlegs/Sade -2005- Forever Best! (2CD)/CD1`
    - `Forever Best! CD2`
        - artist: Sade  ·  year: 2005
        - folder: `D:/music/_cd_rip/Sade (Japan Originals & Remasters) [torrents.ru]/05_Bootlegs/Sade -2005- Forever Best! (2CD)/CD2`

### Forty Licks  (2)
    - `Forty Licks (Disc 1) (CDVD2964)`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/2002 Forty Licks/Forty Licks [ABKCO CDVD2964]/Disc 1`
    - `Forty Licks (Disc 2) (CDVD2964)`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/2002 Forty Licks/Forty Licks [ABKCO CDVD2964]/Disc 2`

### Frantic (UK Of A 2CD set)  (2)
    - `Frantic (UK CD1 Of A 2CD set)`
        - artist: Metallica  ·  year: 2003
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/2003 - Frantic/2003 UK 2CD Set/Disc 1, Vertigo 9811513`
    - `Frantic (UK CD2 Of A 2CD set)`
        - artist: Metallica  ·  year: 2003
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/2003 - Frantic/2003 UK 2CD Set/Disc 2, Vertigo 9811514`

### From The Beginning (2012, 5CD, 88697946622)  (5)
    - `From The Beginning CD1 (2012, 5CD, 88697946622)`
        - artist: Emerson, Lake & Palmer, Atomic Rooster, The Nice  ·  year: 2012
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2007. Emerson, Lake & Palmer - From The Beginning (2012, 5CD, Sony, EU, Austria, 88697946622)/CD1`
    - `From The Beginning CD2 (2012, 5CD, 88697946622)`
        - artist: Emerson, Lake & Palmer  ·  year: 2012
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2007. Emerson, Lake & Palmer - From The Beginning (2012, 5CD, Sony, EU, Austria, 88697946622)/CD2`
    - `From The Beginning CD3 (2012, 5CD, 88697946622)`
        - artist: Emerson, Lake & Palmer  ·  year: 2012
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2007. Emerson, Lake & Palmer - From The Beginning (2012, 5CD, Sony, EU, Austria, 88697946622)/CD3`
    - `From The Beginning CD4 (2012, 5CD, 88697946622)`
        - artist: Emerson, Lake & Palmer  ·  year: 2012
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2007. Emerson, Lake & Palmer - From The Beginning (2012, 5CD, Sony, EU, Austria, 88697946622)/CD4`
    - `From The Beginning CD5 (2012, 5CD, 88697946622)`
        - artist: Emerson, Lake & Palmer  ·  year: 2012
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2007. Emerson, Lake & Palmer - From The Beginning (2012, 5CD, Sony, EU, Austria, 88697946622)/CD5`

### From The Beginning. The Best Of ELP (2011, 2CD, 88697895342)  (2)
    - `From The Beginning. The Best Of ELP CD1 (2011, 2CD, 88697895342)`
        - artist: Emerson, Lake & Palmer  ·  year: 2011
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2011. Emerson, Lake & Palmer - From The Beginning. The Best Of ELP (2011, 2CD, Sony, EU, Austria, 88697895342)/CD1`
    - `From The Beginning. The Best Of ELP CD2 (2011, 2CD, 88697895342)`
        - artist: Emerson, Lake & Palmer  ·  year: 2011
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2011. Emerson, Lake & Palmer - From The Beginning. The Best Of ELP (2011, 2CD, Sony, EU, Austria, 88697895342)/CD2`

### From The Setting Sun... In Wacken (2015, 2CD+DVD, 0210536EMU)  (2)
    - `From The Setting Sun... In Wacken CD1 (2015, 2CD+DVD, 0210536EMU)`
        - artist: Deep Purple  ·  year: 2015
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2015. From The Setting Sun... In Wacken (2015, 2CD+DVD, Edel, Germany, 0210536EMU)/CD1`
    - `From The Setting Sun... In Wacken CD2 (2015, 2CD+DVD, 0210536EMU)`
        - artist: Deep Purple  ·  year: 2015
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2015. From The Setting Sun... In Wacken (2015, 2CD+DVD, Edel, Germany, 0210536EMU)/CD2`

### From The Vault Extra - Live in Japan - Tokyo Dome 1990.2.24  (2)
    - `From The Vault Extra - Live in Japan - Tokyo Dome 1990.2.24 (CD 1)`
        - artist: The Rolling Stones  ·  year: 2017
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2017 From The Vault Extra [RSR GQBS-90274~6]/Disc 1`
    - `From The Vault Extra - Live in Japan - Tokyo Dome 1990.2.24 (CD 2)`
        - artist: The Rolling Stones  ·  year: 2017
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2017 From The Vault Extra [RSR GQBS-90274~6]/Disc 2`

### Garage Inc. [2013 Blackened BLCKND013-2]  (15)
    - `Garage Inc. (Disc 1) [2013 Blackened BLCKND013-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/2013 USA Blackened BLCKND013-2/Disc 1`
    - `Garage Inc. (Disc 1) [Brazil Vertigo 538 351-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 Brazil Vertigo 538 351-2/Disc 1`
    - `Garage Inc. (Disc 1) [Indonesia Vertigo 538 351-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1999 Indonesia Vertigo 538 351-2/Disc 1`
    - `Garage Inc. (Disc 1) [SHM-CD, UICY-94670]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/2010 Japan SHM-CD, UICY-94670~1/Disc 1`
    - `Garage Inc. (Disc 1) [SRCS 8809]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 Japan SRCS 8809~10/Disc 1`
    - `Garage Inc. (Disc 1) [UICR-1060]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/2006 Japan UICR-1060~1/Disc 1`
    - `Garage Inc. (Disc 1) [UK Vertigo 538 351-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 UK Vertigo 538 351-2/Disc 1`
    - `Garage Inc. (Disc 1) [USA Elektra 62299-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 USA Elektra 62299-2/Disc 1`
    - `Garage Inc. (Disc 2) [Brazil Vertigo 538 351-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 Brazil Vertigo 538 351-2/Disc 2`
    - `Garage Inc. (Disc 2) [Indonesia Vertigo 538 352-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1999 Indonesia Vertigo 538 351-2/Disc 2`
    - `Garage Inc. (Disc 2) [SHM-CD, UICY-94671]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/2010 Japan SHM-CD, UICY-94670~1/Disc 2`
    - `Garage Inc. (Disc 2) [SRCS 8810]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 Japan SRCS 8809~10/Disc 2`
    - `Garage Inc. (Disc 2) [UICR-1061]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/2006 Japan UICR-1060~1/Disc 2`
    - `Garage Inc. (Disc 2) [UK Vertigo 538 351-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 UK Vertigo 538 351-2/Disc 2`
    - `Garage Inc. (Disc 2) [USA Elektra 62299-2]`
        - artist: Metallica  ·  year: 1998
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1998 - Garage Inc. (2 CD)/1998 USA Elektra 62299-2/Disc 2`

### Gentleman of Music  (2)
    - `Gentleman of Music: CD1`
        - artist: James Last & His Orchestra  ·  year: 2001
        - folder: `D:/music/JAMES LAST COLLECTION/2001 - Gentleman of Music (Eagle Records 5381912) 2CD`
    - `Gentleman of Music: CD2`
        - artist: James Last & His Orchestra  ·  year: 2001
        - folder: `D:/music/JAMES LAST COLLECTION/2001 - Gentleman of Music (Eagle Records 5381912) 2CD`

### Get Your Sting And Blackout Live 2011  (2)
    - `Get Your Sting And Blackout Live 2011 CD1`
        - artist: Scorpions  ·  year: 2011
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2011 Get Your Sting And Blackout/2011 Get Your Sting And Blackout [2011 Germany 88697 91812 2 Sony]/CD 1`
    - `Get Your Sting And Blackout Live 2011 CD2`
        - artist: Scorpions  ·  year: 2011
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2011 Get Your Sting And Blackout/2011 Get Your Sting And Blackout [2011 Germany 88697 91812 2 Sony]/CD 2`

### Gods Of War Live (MCA 01207-2)  (4)
    - `Gods Of War Live CD 1 (MCA 01207-2)`
        - artist: Manowar  ·  year: 2007
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/2007 Gods Of War Live/2007 Gods Of War Live [2007 Germany MCA 01207-2 MCM]/CD 1`
    - `Gods Of War Live CD 2 (MCA 01207-2)`
        - artist: Manowar  ·  year: 2007
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/2007 Gods Of War Live/2007 Gods Of War Live [2007 Germany MCA 01207-2 MCM]/CD 2`
    - `Gods Of War Live [MICP-90029-A] Disc 1`
        - artist: Manowar  ·  year: 2007
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/2007 Gods Of War Live/2007 Gods Of War Live [2007 Japan MICP-90029 Avalon]/CD 1`
    - `Gods Of War Live [MICP-90029-B] Disc 2`
        - artist: Manowar  ·  year: 2007
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/2007 Gods Of War Live/2007 Gods Of War Live [2007 Japan MICP-90029 Avalon]/CD 2`

### GOLD  (4)
    - `GOLD (Disc One)`
        - artist: KISS  ·  year: 2004
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2004 Gold/2004 Gold [2004 Mercury B0003419-00 USA]/CD 1`
    - `GOLD (Disc Two)`
        - artist: KISS  ·  year: 2004
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Compilations/2004 Gold/2004 Gold [2004 Mercury B0003419-00 USA]/CD 2`
    - `Gold (Disc One)`
        - artist: Cinderella  ·  year: 2006
        - folder: `D:/music/Cinderella -2006- Gold (US. 2CD) [torrents.ru]/CD1`
    - `Gold (Disc Two)`
        - artist: Cinderella  ·  year: 2006
        - folder: `D:/music/Cinderella -2006- Gold (US. 2CD) [torrents.ru]/CD2`

### Golden  (2)
    - `Golden (disc 1)`
        - artist: Romantic Collection  ·  year: 2005
        - folder: `D:/music/_cd_rip/Romantic Collection/02. Golden/Golden_Vol_1`
    - `Golden (disc 2)`
        - artist: Romantic Collection  ·  year: 2005
        - folder: `D:/music/_cd_rip/Romantic Collection/02. Golden/Golden_Vol_2`

### Goodbye Yellow Brick Road  (4)
    - `Goodbye Yellow Brick Road CD1`
        - artist: Elton John  ·  year: 1973
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1973. Goodbye Yellow Brick Road/1973. Elton John - Goodbye Yellow Brick Road (Mercury 375 348-9, EU, 2 CD)/Disc 1`
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1973. Goodbye Yellow Brick Road/1973. Elton John - Goodbye Yellow Brick Road (DJM 821 742-2, Germany)/Disc 1`
    - `Goodbye Yellow Brick Road CD2`
        - artist: Elton John  ·  year: 1973
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1973. Goodbye Yellow Brick Road/1973. Elton John - Goodbye Yellow Brick Road (Mercury 375 348-9, EU, 2 CD)/Disc 2`
    - `Goodbye Yellow Brick Road disc1`
        - artist: Elton John  ·  year: 1973
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1973. Goodbye Yellow Brick Road/1973. Elton John - Goodbye Yellow Brick Road (Carrere 96.087, France)/disc1`
    - `Goodbye Yellow Brick Road disc2`
        - artist: Elton John  ·  year: 1973
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1973. Goodbye Yellow Brick Road/1973. Elton John - Goodbye Yellow Brick Road (Carrere 96.087, France)/disc2`

### Great Instrumentals  (2)
    - `Great Instrumentals - CD1`
        - artist: James Last  ·  year: 1999
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/1999 - Great Instrumentals [Spectrum 107 603-2] 2CD/CD1`
    - `Great Instrumentals - CD2`
        - artist: James Last  ·  year: 1999
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/1999 - Great Instrumentals [Spectrum 107 603-2] 2CD/CD2`

### Greatest Hit (...And 21 Other Pretty Cool Songs)  (2)
    - `Greatest Hit (...And 21 Other Pretty Cool Songs) (Disc 1)`
        - artist: Dream Theater  ·  year: 2008
        - folder: `D:/music/_cd_rip/Dream Theater/Albums/2008-Greatest Hit`
    - `Greatest Hit (...And 21 Other Pretty Cool Songs) (Disc 2)`
        - artist: Dream Theater  ·  year: 2008
        - folder: `D:/music/_cd_rip/Dream Theater/Albums/2008-Greatest Hit`

### Greatest Hits  (6)
    - `Greatest Hits (CD1)`
        - artist: Silent Circle  ·  year: 2008
        - folder: `D:/music/Silent Circle/2008 - Greatest Hits (2CD)/CD1`
    - `Greatest Hits (CD2)`
        - artist: Silent Circle  ·  year: 2008
        - folder: `D:/music/Silent Circle/2008 - Greatest Hits (2CD)/CD2`
    - `Greatest Hits CD01`
        - artist: Bon Jovi  ·  year: 2010
        - folder: `D:/music/_cd_rip/BJ_Discography/03 Compilations/2010-Greatest Hits/CD 01`
    - `Greatest Hits CD02`
        - artist: Bon Jovi  ·  year: 2010
        - folder: `D:/music/_cd_rip/BJ_Discography/03 Compilations/2010-Greatest Hits/CD 02`
    - `Greatest Hits CD1`
        - artist: Bad Boys Blue  ·  year: 2009, 2005
        - folder: `D:/music/Bad Boys Blue - Greatest Hits (2CD) - 2009/CD1`
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2005.Greatest Hits (2 CD)/Greatest Hits CD1`
    - `Greatest Hits CD2`
        - artist: Bad Boys Blue  ·  year: 2009, 2005
        - folder: `D:/music/Bad Boys Blue - Greatest Hits (2CD) - 2009/CD2`
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2005.Greatest Hits (2 CD)/Greatest Hits CD2`

### Greatest Hits 1970-2002  (5)
    - `Greatest Hits 1970-2002 (Disc1)`
        - artist: Elton John  ·  year: 2003
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/2002. Elton John - Greatest Hits 1970-2002 (Mercury 986 557-0, EU, 3CD)/Disc 1`
    - `Greatest Hits 1970-2002 (Disc2)`
        - artist: Elton John  ·  year: 2003
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/2002. Elton John - Greatest Hits 1970-2002 (Mercury 986 557-0, EU, 3CD)/Disc 2`
    - `Greatest Hits 1970-2002 (Disc3)`
        - artist: Elton John  ·  year: 2003
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/2002. Elton John - Greatest Hits 1970-2002 (Mercury 986 557-0, EU, 3CD)/Disc 3`
    - `Greatest Hits 1970-2002 CD1`
        - artist: Elton John  ·  year: 2003
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/2002. Elton John - Greatest Hits 1970-2002 (Mercury 077 012-2, EU, 2CD)/Disc 1`
    - `Greatest Hits 1970-2002 CD2`
        - artist: Elton John  ·  year: 2002
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/2002. Elton John - Greatest Hits 1970-2002 (Mercury 077 012-2, EU, 2CD)/Disc 2`

### Greatest Hits Live-in Concert on Air 1989-1991  (4)
    - `Greatest Hits Live-in Concert on Air 1989-1991 CD1`
        - artist: Guns N' Roses  ·  year: 2016
        - folder: `D:/music/Guns N' Roses - 2016 - Greatest Hits Live On Air 1989-'91/CD1`
    - `Greatest Hits Live-in Concert on Air 1989-1991 CD2`
        - artist: Guns And Roses  ·  year: 2016
        - folder: `D:/music/Guns N' Roses - 2016 - Greatest Hits Live On Air 1989-'91/CD2`
    - `Greatest Hits Live-in Concert on Air 1989-1991 CD3`
        - artist: Guns N' Roses  ·  year: 2016
        - folder: `D:/music/Guns N' Roses - 2016 - Greatest Hits Live On Air 1989-'91/CD3`
    - `Greatest Hits Live-in Concert on Air 1989-1991 CD4`
        - artist: Guns N' Roses  ·  year: 2016
        - folder: `D:/music/Guns N' Roses - 2016 - Greatest Hits Live On Air 1989-'91/CD4`

### Greatest Remix Hits Volume 1 [Japanese CD Album]  (2)
    - `Greatest Remix Hits Volume 1 (CD1) [Japanese CD Album]`
        - artist: Kylie Minogue  ·  year: 1993
        - folder: `D:/music/_cd_rip/Kylie Minogue Japan CDs/Kylie Minogue - 1993 - Greatest Remix Hits Vol. 1 (2CD) (WMC5-681-2)/CD1`
    - `Greatest Remix Hits Volume 1 (CD2) [Japanese CD Album]`
        - artist: Kylie Minogue  ·  year: 1993
        - folder: `D:/music/_cd_rip/Kylie Minogue Japan CDs/Kylie Minogue - 1993 - Greatest Remix Hits Vol. 1 (2CD) (WMC5-681-2)/CD2`

### Greatest Remix Hits Volume 2  (2)
    - `Greatest Remix Hits Volume 2 (CD1)`
        - artist: Kylie Minogue  ·  year: 1998
        - folder: `D:/music/_cd_rip/Kylie Minogue Japan CDs/Kylie Minogue - 1993 - Greatest Remix Hits Vol. 2 (2CD) (WMC5-683-4)/CD1`
    - `Greatest Remix Hits Volume 2 (CD2)`
        - artist: Kylie Minogue  ·  year: 1998
        - folder: `D:/music/_cd_rip/Kylie Minogue Japan CDs/Kylie Minogue - 1993 - Greatest Remix Hits Vol. 2 (2CD) (WMC5-683-4)/CD2`

### GRRR! [ABKCO 3710914]  (3)
    - `GRRR! (Disc 1) [ABKCO 3710914]`
        - artist: The Rolling Stones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/2012 Grrr!/Grrr! [ABKCO 3710914]/Disc 1`
    - `GRRR! (Disc 2)  [ABKCO 3710914]`
        - artist: The Rolling Stones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/2012 Grrr!/Grrr! [ABKCO 3710914]/Disc 2`
    - `GRRR! (Disc 3) [ABKCO 3710914]`
        - artist: The Rolling Stones  ·  year: 2012
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/2012 Grrr!/Grrr! [ABKCO 3710914]/Disc 3`

### Hammered (Germany, Steamhammer, SPV 089-74060 DCD)  (2)
    - `Hammered (Germany, Steamhammer, SPV 089-74060 DCD) (CD1)`
        - artist: Motorhead  ·  year: 2002
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/2002-Hammered/2002-Hammered (2002, Germany, Steamhammer, SPV 089-74060 DCD, 2CD)/CD1`
    - `Hammered (Germany, Steamhammer, SPV 089-74060 DCD) (CD2)`
        - artist: Motorhead  ·  year: 2002
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/2002-Hammered/2002-Hammered (2002, Germany, Steamhammer, SPV 089-74060 DCD, 2CD)/CD2`

### Hampton Coliseum - Live in 1981  (2)
    - `Hampton Coliseum - Live in 1981 [disc 1]`
        - artist: Rolling Stones  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2012 Hampton Coliseum (Live In 1981) [Promotone EAGDV037]/Disc 1`
    - `Hampton Coliseum - Live in 1981 [disc 2]`
        - artist: Rolling Stones  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2012 Hampton Coliseum (Live In 1981) [Promotone EAGDV037]/Disc 2`

### Handbags & Gladrags  (2)
    - `Handbags & Gladrags (CD1)`
        - artist: Rod Stewart, Stewart, Rod  ·  year: 1995
        - folder: `D:/music/_cd_rip/ROD STEWART/Rod Stewart - 1995 - Handbags & Gladrags (2CD) (Mercury 528 823-2 2005)/CD1`
    - `Handbags & Gladrags (CD2)`
        - artist: Rod Stewart  ·  year: 1995
        - folder: `D:/music/_cd_rip/ROD STEWART/Rod Stewart - 1995 - Handbags & Gladrags (2CD) (Mercury 528 823-2 2005)/CD2`

### Havana Moon (Live)  (2)
    - `Havana Moon (Live) (CD1)`
        - artist: The Rolling Stones  ·  year: 2016
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2016 Havana Moon [Eagle EAGDV065]/Disc 1`
    - `Havana Moon (Live) (CD2)`
        - artist: The Rolling Stones  ·  year: 2016
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2016 Havana Moon [Eagle EAGDV065]/Disc 2`

### Heartbreak Station [PHDR-18]  (3)
    - `Heartbreak Station (CDS) [PHDR-18]`
        - artist: Cinderella  ·  year: 1991
        - folder: `D:/music/Cinderella/1991 Heartbreak Station (CDS)`
    - `Heartbreak Station [disc 01]`
        - artist: Cinderella  ·  year: 2011
        - folder: `D:/music/Cinderella - 1990 - Heartbreak Station {2011 Remaster BAD110601}/disc 01`
    - `Heartbreak Station [disc 02]`
        - artist: Cinderella  ·  year: 2011
        - folder: `D:/music/Cinderella - 1990 - Heartbreak Station {2011 Remaster BAD110601}/disc 02`

### Heaven And Hell (Deluxe Expanded Edition)  (2)
    - `Heaven And Hell (Deluxe Expanded Edition, CD1)`
        - artist: Black Sabbath  ·  year: 1980
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1980 Heaven And Hell/1980 Heaven And Hell [2010 Germany 2735073 Sanctuary]/CD1`
    - `Heaven And Hell (Deluxe Expanded Edition, CD2)`
        - artist: Black Sabbath  ·  year: 1980
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1980 Heaven And Hell/1980 Heaven And Hell [2010 Germany 2735073 Sanctuary]/CD2`

### Heaven On Earth (Remastered & Expanded Special Edition)  (2)
    - `Heaven On Earth (Remastered & Expanded Special Edition) CD 1`
        - artist: Belinda Carlisle  ·  year: 1987
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1987 Heaven On Earth (2013, Edsel Records, EDSG 8025)/CD1`
    - `Heaven On Earth (Remastered & Expanded Special Edition) CD 2`
        - artist: Belinda Carlisle  ·  year: 1987
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1987 Heaven On Earth (2013, Edsel Records, EDSG 8025)/CD2`

### Heavy ConstruKction [DGM0013]  (3)
    - `Heavy ConstruKction [DGM0013] CD1`
        - artist: King Crimson  ·  year: 2000
        - folder: `D:/music/King Crimson/3.Live/2000. Heavy ConstruKction/2000, US, Discipline Global Mobile, DGM0013/CD 1`
    - `Heavy ConstruKction [DGM0013] CD2`
        - artist: King Crimson  ·  year: 2000
        - folder: `D:/music/King Crimson/3.Live/2000. Heavy ConstruKction/2000, US, Discipline Global Mobile, DGM0013/CD 2`
    - `Heavy ConstruKction [DGM0013] CD3`
        - artist: King Crimson  ·  year: 2000
        - folder: `D:/music/King Crimson/3.Live/2000. Heavy ConstruKction/2000, US, Discipline Global Mobile, DGM0013/CD 3`

### Hell On Stage Live [VICP-60674]  (2)
    - `Hell On Stage Live [VICP-60674] Disc 1`
        - artist: Manowar  ·  year: 1999
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1999 Hell On Stage Live/1999 Hell On Stage Live [1999 Japan VICP-60674~75 Victor]/CD 1`
    - `Hell On Stage Live [VICP-60675] Disc 2`
        - artist: Manowar  ·  year: 1999
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1999 Hell On Stage Live/1999 Hell On Stage Live [1999 Japan VICP-60674~75 Victor]/CD 2`

### Hell On Stage Live - (3984-14254-2)  (4)
    - `Hell On Stage Live - CD 1 (3984-14254-2)`
        - artist: Manowar  ·  year: 1999
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1999 Hell On Stage Live/1999 Hell On Stage Live [1999 USA 3984-14254-2 Metal Blade]/CD 1`
    - `Hell On Stage Live - CD 2 (3984-14254-2)`
        - artist: Manowar  ·  year: 1999
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1999 Hell On Stage Live/1999 Hell On Stage Live [1999 USA 3984-14254-2 Metal Blade]/CD 2`
    - `Hell On Stage Live - Disc 1 (3048902)`
        - artist: Manowar  ·  year: 1999
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1999 Hell On Stage Live/1999 Hell On Stage Live [1999 France 3048912 Wagram]/CD 1`
    - `Hell On Stage Live - Disc 2 (3048902)`
        - artist: Manowar  ·  year: 1999
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1999 Hell On Stage Live/1999 Hell On Stage Live [1999 France 3048912 Wagram]/CD 2`

### Hell On Wheels Live [VICP-60278]  (2)
    - `Hell On Wheels Live [VICP-60278] Disc1`
        - artist: Manowar  ·  year: 1998
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1997 Hell On Wheels Live/1997 Hell On Wheels Live [1998 Japan VICP-60278~79 Victor]/CD 1`
    - `Hell On Wheels Live [VICP-60279] Disc 2`
        - artist: Manowar  ·  year: 1998
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1997 Hell On Wheels Live/1997 Hell On Wheels Live [1998 Japan VICP-60278~79 Victor]/CD 2`

### Hell On Wheels: Live  (4)
    - `Hell On Wheels: Live (Disc 1)`
        - artist: Manowar  ·  year: 1997
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1997 Hell On Wheels Live/1997 Hell On Wheels Live [1997 USA MB 3984-14257-2  Metal Blade]/CD 1`
    - `Hell On Wheels: Live (Disc 2)`
        - artist: Manowar  ·  year: 1997
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1997 Hell On Wheels Live/1997 Hell On Wheels Live [1997 USA MB 3984-14257-2  Metal Blade]/CD 2`
    - `Hell On Wheels: Live Disc 1 (UMD 70062)`
        - artist: Manowar  ·  year: 1997
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1997 Hell On Wheels Live/1997 Hell On Wheels Live [1997 Spain UMD 70062 Universal]/CD 1`
    - `Hell On Wheels: Live Disc 2 (UMD 70062)`
        - artist: Manowar  ·  year: 1997
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Live Albums/1997 Hell On Wheels Live/1997 Hell On Wheels Live [1997 Spain UMD 70062 Universal]/CD 2`

### Here And There (314-528 164-2)  (4)
    - `Here And There (Disc 1) (314-528 164-2)`
        - artist: Elton John  ·  year: 1995
        - folder: `D:/music/_cd_rip/Sir Elton John/Live/1976. Here And There/1976. Elton John - Here And There (Rocket 314-528 164-2, USA)/Disc 1`
    - `Here And There (Disc 2) (314-528 164-2)`
        - artist: Elton John  ·  year: 1995
        - folder: `D:/music/_cd_rip/Sir Elton John/Live/1976. Here And There/1976. Elton John - Here And There (Rocket 314-528 164-2, USA)/Disc 2`
    - `Here And There Disc 1`
        - artist: Elton John  ·  year: 1976
        - folder: `D:/music/_cd_rip/Sir Elton John/Live/1976. Here And There/1976. Elton John - Here And There (This Record 528 164-2, Germany)/Disc 1`
    - `Here And There [Disc 2]`
        - artist: Elton John  ·  year: 1976
        - folder: `D:/music/_cd_rip/Sir Elton John/Live/1976. Here And There/1976. Elton John - Here And There (This Record 528 164-2, Germany)/Disc 2`

### Hero Of The Day (Australia Part One)  (2)
    - `Hero Of The Day (Australia Part  One)`
        - artist: Metallica  ·  year: 1996
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/1996 - Hero Of The Day/1996 Australia Part One, Mercury 578575 2`
    - `Hero Of The Day (Part One of  a Two CD set)`
        - artist: Metallica  ·  year: 1996
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/1996 - Hero Of The Day/1996 UK 2CD Set/Part 1, UK METCD 13`

### High Voltage Festival (2010, 2CD, CLCD286)  (2)
    - `High Voltage Festival CD1 (2010, 2CD, CLCD286)`
        - artist: Emerson, Lake & Palmer  ·  year: 2010
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2010. Emerson, Lake & Palmer - High Voltage Festival (2010, 2CD, Concert Live, EU, UK, CLCD286)/CD1`
    - `High Voltage Festival CD2 (2010, 2CD, CLCD286)`
        - artist: Emerson, Lake & Palmer  ·  year: 2010
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2010. Emerson, Lake & Palmer - High Voltage Festival (2010, 2CD, Concert Live, EU, UK, CLCD286)/CD2`

### Highway To Hell  (2)
    - `Highway To Hell (CD 1)`
        - artist: AC/DC  ·  year: 1992
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Singles & Promos/1992 - Highway To Hell (UK 2 CD Set Single)/CD1`
    - `Highway To Hell (CD 2)`
        - artist: AC/DC  ·  year: 1992
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Singles & Promos/1992 - Highway To Hell (UK 2 CD Set Single)/CD2`

### Hit Collection  (3)
    - `Hit Collection CD1`
        - artist: Bad Boys Blue  ·  year: 2006
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2006.Hit Collection (3 CD)/Hit Collection CD1`
    - `Hit Collection CD2`
        - artist: Bad Boys Blue  ·  year: 2006
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2006.Hit Collection (3 CD)/Hit Collection CD2`
    - `Hit Collection CD3`
        - artist: Bad Boys Blue  ·  year: 2006
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2006.Hit Collection (3 CD)/Hit Collection CD3`

### Holy Diver (2012, Deluxe Expanded Edition)  (3)
    - `Holy Diver (2012, Deluxe Expanded Edition, Disc 1)`
        - artist: Dio  ·  year: 1983
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1983 Holy Diver/1983 Holy Diver [2012 Universal 5337835 Germany]/Dics 1 - Holy Diver`
    - `Holy Diver (2012, Deluxe Expanded Edition, Disc 2)`
        - artist: Dio  ·  year: 1983
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1983 Holy Diver/1983 Holy Diver [2012 Universal 5337835 Germany]/Disc 2 - Bonus Tracks`
    - `Holy Diver (The Singles Collection, Disc 1)`
        - artist: Dio  ·  year: 2012
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Box Set/2012 The Singles Box Set [Universal 006025 2799275 4 EU]/Disc 1 - Holy Diver (CDS, 00602537091355)`

### Holy Diver Live  (4)
    - `Holy Diver Live CD1`
        - artist: Dio  ·  year: 2006
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/2006 Holy Diver Live/2006 Holy Diver Live [2006 Victor VICP-63531~2 Japan]/CD 1`
    - `Holy Diver Live CD2`
        - artist: Dio  ·  year: 2006
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/2006 Holy Diver Live/2006 Holy Diver Live [2006 Victor VICP-63531~2 Japan]/CD 2`
    - `Holy Diver Live Disc 1 (EDGCD324)`
        - artist: Dio  ·  year: 2006
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/2006 Holy Diver Live/2006 Holy Diver Live [2006 Eagle EDGCD324 Germany]/CD 1`
    - `Holy Diver Live Disc 2 (EDGCD324)`
        - artist: Dio  ·  year: 2006
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Live Albums/2006 Holy Diver Live/2006 Holy Diver Live [2006 Eagle EDGCD324 Germany]/CD 2`

### Honk (Deluxe)  (2)
    - `Honk (Deluxe) (CD1)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/2019 Honk [773 188-0]/CD1`
    - `Honk (Deluxe) (CD2)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/2019 Honk [773 188-0]/CD2`

### Hot Rocks 1964 - 1971 (882 334-2)  (2)
    - `Hot Rocks 1964 - 1971 (Disc 1) (882 334-2)`
        - artist: The Rolling Stones  ·  year: 1971
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1971 Hot Rocks 1964-1971/Hot Rocks 1964-1971 [ABKCO 882334-2]/Disc 1`
    - `Hot Rocks 1964 - 1971 (Disc 2) (882 334-2)`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1971 Hot Rocks 1964-1971/Hot Rocks 1964-1971 [ABKCO 882334-2]/Disc 2`

### Hot Rocks 1964-1971 (66672)  (4)
    - `Hot Rocks 1964-1971 (Disc 1) (66672)`
        - artist: The Rolling Stones  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1971 Hot Rocks 1964-1971/Hot Rocks 1964-1971 [ABKCO 66672]/Disc 1`
    - `Hot Rocks 1964-1971 (Disc 2) (66672)`
        - artist: The Rolling Stones  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1971 Hot Rocks 1964-1971/Hot Rocks 1964-1971 [ABKCO 66672]/Disc 2`
    - `Hot Rocks 1964-1971 CD1 [820 140-2 Australia]`
        - artist: The Rolling Stones  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1971 Hot Rocks 1964-1971/Hot Rocks 1964-1971 [London 820 140-2]/Disc 1`
    - `Hot Rocks 1964-1971 CD2 [820 140-2 Australia]`
        - artist: The Rolling Stones  ·  year: 1986
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1971 Hot Rocks 1964-1971/Hot Rocks 1964-1971 [London 820 140-2]/Disc 2`

### How The West Was Won  (6)
    - `How The West Was Won (CD 1)`
        - artist: Led Zeppelin  ·  year: 2003
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic 7567-83587-2, Germany)/disc1`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic WPCR-11585-7, Japan)/disc1`
    - `How The West Was Won (Disc 3)`
        - artist: Led Zeppelin  ·  year: 2003
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic 7567-83587-2, Germany)/disc3`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic WPCR-11585-7, Japan)/disc3`
    - `How The West Was Won CD1`
        - artist: Led Zeppelin  ·  year: 2003
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic 0603497862788, EU)/CD1`
    - `How The West Was Won CD2`
        - artist: Led Zeppelin  ·  year: 2003
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic 0603497862788, EU)/CD2`
    - `How The West Was Won CD3`
        - artist: Led Zeppelin  ·  year: 2003
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic 0603497862788, EU)/CD3`
    - `How the West Was Won (CD 2)`
        - artist: Led Zeppelin  ·  year: 1972
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic 7567-83587-2, Germany)/disc2`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/2003. How West Was Won/2003. Led Zeppelin - How West Was Won (Atlantic WPCR-11585-7, Japan)/disc2`

### Hunting High And Low (Deluxe Edition)  (2)
    - `Hunting High And Low (Deluxe Edition) (Disc 1)`
        - artist: a-ha  ·  year: 2010
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Hunting High And Low (Deluxe Edition) (Japan 2CD)/CD1`
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Hunting High And Low (Remastered & Expanded) 2CD/CD1`
    - `Hunting High And Low (Deluxe Edition) (Disc 2)`
        - artist: a-ha  ·  year: 2010
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Hunting High And Low (Deluxe Edition) (Japan 2CD)/CD2`
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Hunting High And Low (Remastered & Expanded) 2CD/CD2`

### Hydra (Deluxe Edition)  (2)
    - `Hydra (Deluxe Edition) CD 1`
        - artist: Within Temptation  ·  year: 2014
        - folder: `D:/music/_cd_rip/Within Temptation - Дискография/Albums/Within Temptation - 2014 - Hydra (Deluxe Edition) (2CD)/Within Temptation-Hydra (Deluxe Edition) CD 1-2014`
    - `Hydra (Deluxe Edition) CD 2`
        - artist: Within Temptation  ·  year: 2014
        - folder: `D:/music/_cd_rip/Within Temptation - Дискография/Albums/Within Temptation - 2014 - Hydra (Deluxe Edition) (2CD)/Within Temptation-Hydra (Deluxe Edition) CD 2-2014`

### I Like... Live!  (2)
    - `I Like... Live! CD 1`
        - artist: Gazebo  ·  year: 2013
        - folder: `D:/music/Gazebo - I like... live! (2013) lossless/CD 1`
    - `I Like... Live! CD 2`
        - artist: Gazebo  ·  year: 2013
        - folder: `D:/music/Gazebo - I like... live! (2013) lossless/CD 2`

### In 80 Jahren um die Welt  (2)
    - `In 80 Jahren um die Welt - CD2`
        - artist: James Last  ·  year: 2008
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2008 - Mit 80 Jahren um die Welt [Universal Music 06007 5312995 (1)] 4CD/CD2`
    - `In 80 Jahren um die Welt - CD4`
        - artist: James Last  ·  year: 2008
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2008 - Mit 80 Jahren um die Welt [Universal Music 06007 5312995 (1)] 4CD/CD4`

### In Absentia  (2)
    - `In Absentia CD1`
        - artist: Porcupine Tree  ·  year: 2002
        - folder: `D:/music/Porcupine Tree Main discography/Original Albums/2003 - In Absentia 2CD (European Special Edition) (Lava 7567 93163-2 Europe)/CD1`
    - `In Absentia CD2`
        - artist: Porcupine Tree  ·  year: 2002
        - folder: `D:/music/Porcupine Tree Main discography/Original Albums/2003 - In Absentia 2CD (European Special Edition) (Lava 7567 93163-2 Europe)/CD2`

### In And Out Of Consciousness  (2)
    - `In And Out Of Consciousness (Disc Three)`
        - artist: Robbie Williams  ·  year: 2010
        - folder: `D:/music/_cd_rip/Robbie Williams/2010 - In And Out Of Consciousness (Deluxe Edition)/CD3`
    - `In And Out Of Consciousness (Disc Two)`
        - artist: Robbie Williams  ·  year: 2010
        - folder: `D:/music/_cd_rip/Robbie Williams/2010 - In And Out Of Consciousness (Deluxe Edition)/CD2`

### In Concert (1970 - 1972) (1992, 2CD, CDS7981812)  (2)
    - `In Concert (1970 - 1972) CD1 (1992, 2CD, CDS7981812)`
        - artist: Deep Purple  ·  year: 1980
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1980. Deep Purple In Concert (1970-1972) (1992, 2CD, EMI, EU, Poland, CDS7981812)/CD1`
    - `In Concert (1970 - 1972) CD2 (1992, 2CD, CDS7981812)`
        - artist: Deep Purple  ·  year: 1980
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1980. Deep Purple In Concert (1970-1972) (1992, 2CD, EMI, EU, Poland, CDS7981812)/CD2`

### In Concert With The London Symphony Orchestra (1999, 2CD, EAGCD124)  (2)
    - `In Concert With The London Symphony Orchestra CD1 (1999, 2CD, EAGCD124)`
        - artist: Deep Purple  ·  year: 1999
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1999. In Concert With The London Symphony Orchestra (1999, 2CD, Eagle, EC, Germany, EAGCD124)/CD1`
    - `In Concert With The London Symphony Orchestra CD2 (1999, 2CD, EAGCD124)`
        - artist: Deep Purple  ·  year: 1999
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1999. In Concert With The London Symphony Orchestra (1999, 2CD, Eagle, EC, Germany, EAGCD124)/CD2`

### In No Sense?.. Nonsense!.. (Deluxe Edition)  (2)
    - `In No Sense?.. Nonsense!.. (Deluxe Edition) (Disc One)`
        - artist: The Art of Noise  ·  year: 2018
        - folder: `D:/music/The Art of Noise - In no sense Nonsense (2018)/CD1`
    - `In No Sense?.. Nonsense!.. (Deluxe Edition) (Disc Two)`
        - artist: The Art of Noise  ·  year: 1987
        - folder: `D:/music/The Art of Noise - In no sense Nonsense (2018)/CD2`

### In the Absence of Pink. Knebworth 85 (1991, 2CD, DPVSOPCD163)  (2)
    - `In the Absence of Pink. Knebworth 85 CD1 (1991, 2CD, DPVSOPCD163)`
        - artist: Deep Purple  ·  year: 1985
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1991. In The Absence Of Pink. Knebworth 85 (1991, 2CD, Connoisseur Collection, EU, UK, DPVSOPCD163)/CD1`
    - `In the Absence of Pink. Knebworth 85 CD2 (1991, 2CD, DPVSOPCD163)`
        - artist: Deep Purple  ·  year: 1991
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1991. In The Absence Of Pink. Knebworth 85 (1991, 2CD, Connoisseur Collection, EU, UK, DPVSOPCD163)/CD2`

### In The Court Of The Crimson King [2009, KCCBX1]  (5)
    - `In The Court Of The Crimson King [2009, KCCBX1] CD1`
        - artist: King Crimson  ·  year: 1969
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1969. In The Court Of The Crimson King/2009, UK, Discipline Global Mobile, KCCBX1/CD 1`
    - `In The Court Of The Crimson King [2009, KCCBX1] CD2`
        - artist: King Crimson  ·  year: 1969
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1969. In The Court Of The Crimson King/2009, UK, Discipline Global Mobile, KCCBX1/CD 2`
    - `In The Court Of The Crimson King [2009, KCCBX1] CD3`
        - artist: King Crimson  ·  year: 1969
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1969. In The Court Of The Crimson King/2009, UK, Discipline Global Mobile, KCCBX1/CD 3`
    - `In The Court Of The Crimson King [2009, KCCBX1] CD4`
        - artist: King Crimson  ·  year: 1969
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1969. In The Court Of The Crimson King/2009, UK, Discipline Global Mobile, KCCBX1/CD 4`
    - `In The Court Of The Crimson King [2009, KCCBX1] CD5`
        - artist: King Crimson  ·  year: 1969
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1969. In The Court Of The Crimson King/2009, UK, Discipline Global Mobile, KCCBX1/CD 5`

### In The Flesh  (2)
    - `In The Flesh (disc 1)`
        - artist: Roger Waters  ·  year: 2000
        - folder: `D:/music/Pink Floyd - Solo Albums (lossless)/Roger Waters/2000 - In The Flesh (Live)/Disc-1`
    - `In The Flesh (disc 2)`
        - artist: Roger Waters  ·  year: 2000
        - folder: `D:/music/Pink Floyd - Solo Albums (lossless)/Roger Waters/2000 - In The Flesh (Live)/Disc-2`

### In Visbile Silence - Deluxe Edition  (2)
    - `In Visbile Silence - Deluxe Edition (Disc 1)`
        - artist: The Art of Noise  ·  year: 2017
        - folder: `D:/music/The Art of Noise - In Visible Silence (Deluxe edition)/CD1`
    - `In Visbile Silence - Deluxe Edition (Disc 2)`
        - artist: The Art of Noise  ·  year: 2017
        - folder: `D:/music/The Art of Noise - In Visible Silence (Deluxe edition)/CD2`

### Iron Maiden (7243 8 35868 2 0)  (4)
    - `Iron Maiden (7243 8 35868 2 0, CD1)`
        - artist: Iron Maiden  ·  year: 1980
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1980 Iron Maiden/1980 Iron Maiden [1995 Holland 7243 8 35868 2 0 EMI]/CD 1`
    - `Iron Maiden (7243 8 35868 2 0, CD2)`
        - artist: Iron Maiden  ·  year: 1980
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1980 Iron Maiden/1980 Iron Maiden [1995 Holland 7243 8 35868 2 0 EMI]/CD 2`
    - `Iron Maiden [Castle 102-2] CD 1`
        - artist: Iron Maiden  ·  year: 1980
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1980 Iron Maiden/1980 Iron Maiden [1995 USA CASTLE 102-2. Castle Records]/CD 1`
    - `Iron Maiden [Castle 102-2] CD 2`
        - artist: Iron Maiden  ·  year: 1980
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1980 Iron Maiden/1980 Iron Maiden [1995 USA CASTLE 102-2. Castle Records]/CD 2`

### Is There Anybody Out There  (2)
    - `Is There Anybody Out There (Disc 1)`
        - artist: Pink Floyd  ·  year: 2000
        - folder: `D:/music/Pink Floyd/Live/2000 Is There Anybody Out There/[C2K 62055] [USA, Rem 2004, K-Matrix]/CD1`
    - `Is There Anybody Out There (Disc 2)`
        - artist: Pink Floyd  ·  year: 2000
        - folder: `D:/music/Pink Floyd/Live/2000 Is There Anybody Out There/[C2K 62055] [USA, Rem 2004, K-Matrix]/CD2`

### Is There Anybody Out There? (The Wall Live 1980-81)  (2)
    - `Is There Anybody Out There? (The Wall Live 1980-81) CD1`
        - artist: Pink Floyd  ·  year: 2000
        - folder: `D:/music/Pink Floyd/Live/2000 Is There Anybody Out There/(EMI 7243 5 23562 2 5) [EU, Deluxe edition]/CD1`
    - `Is There Anybody Out There? (The Wall Live 1980-81) CD2`
        - artist: Pink Floyd  ·  year: 2000
        - folder: `D:/music/Pink Floyd/Live/2000 Is There Anybody Out There/(EMI 7243 5 23562 2 5) [EU, Deluxe edition]/CD2`

### IT'SNAZ (2011 Ramaster)  (2)
    - `IT'SNAZ (2011 Ramaster) CD1`
        - artist: Nazareth  ·  year: 1981
        - folder: `D:/music/_cd_rip/Nazareth discography/Live/1981. Nazareth - 'Snaz/1981. Nazareth - 'Snaz (Salvo, SALVOMDCD18,EU)/CD1`
    - `IT'SNAZ (2011 Ramaster) CD2`
        - artist: Nazareth  ·  year: 1981
        - folder: `D:/music/_cd_rip/Nazareth discography/Live/1981. Nazareth - 'Snaz/1981. Nazareth - 'Snaz (Salvo, SALVOMDCD18,EU)/CD2`

### James Last in Holland  (3)
    - `James Last in Holland (Disc 1)`
        - artist: James Last  ·  year: 2011
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2011 - James Last in Holland [Universal Music 532 549-0] 3CD/CD1`
    - `James Last in Holland (Disc 2)`
        - artist: James Last  ·  year: 2011
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2011 - James Last in Holland [Universal Music 532 549-0] 3CD/CD2`
    - `James Last in Holland (Disc 3)`
        - artist: James Last  ·  year: 2011
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2011 - James Last in Holland [Universal Music 532 549-0] 3CD/CD3`

### Jewel Box  (8)
    - `Jewel Box CD1`
        - artist: Elton John, Elton John / Leon Russell, Little Richard & Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD1. Deep Cuts - Part one`
    - `Jewel Box CD2`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD2. Deep Cuts - Part two`
    - `Jewel Box CD3`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD3. Rarities - Part one - 1965-68`
    - `Jewel Box CD4`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD4. Rarities - Part two - 1968`
    - `Jewel Box CD5`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD5. Rarities - Part three - 1968-71`
    - `Jewel Box CD6`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD6. B-Sides - Part one - 1976-1984`
    - `Jewel Box CD7`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD7. B-Sides - Part two - 1984-2006`
    - `Jewel Box CD8`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2020. Elton John - Jewel Box (Universal 071 590-8, EU)/CD8. And This Is Me`

### Join Together  (2)
    - `Join Together (Disc 1)`
        - artist: The Who  ·  year: 1990
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1990 - Join Together (2 CD Virgin Records CDVDT 102)/Disc 1`
    - `Join Together (Disc 2)`
        - artist: The Who  ·  year: 1990
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1990 - Join Together (2 CD Virgin Records CDVDT 102)/Disc 2`

### Killers (7243 8 35869 2 9)  (3)
    - `Killers (7243 8 35869 2 9, CD1)`
        - artist: Iron Maiden  ·  year: 1981
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1981 Killers/1981 Killers [1995 Holland 7243 8 35869 2 9 EMI]/CD 1`
    - `Killers (7243 8 35869 2 9, CD2)`
        - artist: Iron Maiden  ·  year: 1981
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1981 Killers/1981 Killers [1995 Holland 7243 8 35869 2 9 EMI]/CD 2`
    - `Killers [Castle 103-2] CD 2`
        - artist: Iron Maiden  ·  year: 1981
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1981 Killers/1981 Killers [1995 USA CASTLE 103-2. Castle Records]/CD 2`

### Kings Of Metal MMXIV (MCA 01254-2)  (2)
    - `Kings Of Metal MMXIV (MCA 01254-2) Disc 1`
        - artist: Manowar  ·  year: 2014
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Studio Albums/1988 Kings Of Metal/2014 Kings Of Metal [2014 EU MCA 01254-2 MCM]/CD 1`
    - `Kings Of Metal MMXIV (MCA 01254-2) Disc 2`
        - artist: Manowar  ·  year: 2014
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/Studio Albums/1988 Kings Of Metal/2014 Kings Of Metal [2014 EU MCA 01254-2 MCM]/CD 2`

### Knuffelrock 01  (2)
    - `Knuffelrock 01 (Disc 1)`
        - artist: Various Artists  ·  year: 1992
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1992. VA - KnuffelRock (2 CD)/Disc 1`
    - `Knuffelrock 01 (Disc 2)`
        - artist: Various Artists  ·  year: 1992
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1992. VA - KnuffelRock (2 CD)/Disc 2`

### Knuffelrock 02  (2)
    - `Knuffelrock 02 (Disc 1)`
        - artist: Various Artists  ·  year: 1993
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1993. VA - Knuffelrock 2 (2 CD)/Disc 1`
    - `Knuffelrock 02 (Disc 2)`
        - artist: Various Artists  ·  year: 1993
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1993. VA - Knuffelrock 2 (2 CD)/Disc 2`

### Knuffelrock 03  (2)
    - `Knuffelrock 03 (Disc 1)`
        - artist: Various Artists  ·  year: 1994
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1994. VA - Knuffelrock 3 (2 CD)/Disc 1`
    - `Knuffelrock 03 (Disc 2)`
        - artist: Various Artists  ·  year: 1994
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1994. VA - Knuffelrock 3 (2 CD)/Disc 2`

### Knuffelrock 04  (2)
    - `Knuffelrock 04 (Disc 1)`
        - artist: Various Artists  ·  year: 1995
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1995. VA - Knuffelrock 4 (2 CD)/Disc 1`
    - `Knuffelrock 04 (Disc 2)`
        - artist: Various Artists  ·  year: 1995
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1995. VA - Knuffelrock 4 (2 CD)/Disc 2`

### Knuffelrock 06  (2)
    - `Knuffelrock 06 (Disc 1)`
        - artist: Various Artists  ·  year: 1997
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1997. VA - Knuffelrock 6 (2 CD)/Disc 1`
    - `Knuffelrock 06 (Disc 2)`
        - artist: Various Artists  ·  year: 1997
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1997. VA - Knuffelrock 6 (2 CD)/Disc 2`

### Knuffelrock 07  (2)
    - `Knuffelrock 07 (Disc 1)`
        - artist: Various Artists  ·  year: 1998
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1998. VA - Knuffelrock 7 (2 CD)/Disc 1`
    - `Knuffelrock 07 (Disc 2)`
        - artist: Various Artists  ·  year: 1998
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1998. VA - Knuffelrock 7 (2 CD)/Disc 2`

### Knuffelrock 08  (2)
    - `Knuffelrock 08 (Disc 1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1999. VA - Knuffelrock 8 (2 CD)/Disc 1`
    - `Knuffelrock 08 (Disc 2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/VA - KNUFFELROCK - Collection/1999. VA - Knuffelrock 8 (2 CD)/Disc 2`

### Knuffelrock 09  (2)
    - `Knuffelrock 09 (Disc 1)`
        - artist: Various Artists  ·  year: 2000
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2000. VA - Knuffelrock 9 (2 CD)/Disc 1`
    - `Knuffelrock 09 (Disc 2)`
        - artist: Various Artists  ·  year: 2000
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2000. VA - Knuffelrock 9 (2 CD)/Disc 2`

### Knuffelrock 10  (2)
    - `Knuffelrock 10 (Disc 1)`
        - artist: Various Artists  ·  year: 2001
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2001. VA - Knuffelrock 10 (2 CD)/Disc 1`
    - `Knuffelrock 10 (Disc 2)`
        - artist: Various Artists  ·  year: 2001
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2001. VA - Knuffelrock 10 (2 CD)/Disc 2`

### Knuffelrock 11  (2)
    - `Knuffelrock 11 (Disc 1)`
        - artist: Various Artists  ·  year: 2002
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2002. VA - Knuffelrock 11 (2 CD)/Disc 1`
    - `Knuffelrock 11 (Disc 2)`
        - artist: Various Artists  ·  year: 2002
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2002. VA - Knuffelrock 11 (2 CD)/Disc 2`

### Knuffelrock 12  (2)
    - `Knuffelrock 12 (Disc 1)`
        - artist: Various Artists  ·  year: 2003
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2003. VA - Knuffelrock 12 (2 CD)/Disc 1`
    - `Knuffelrock 12 (Disc 2)`
        - artist: Various Artists  ·  year: 2003
        - folder: `D:/music/VA - KNUFFELROCK - Collection/2003. VA - Knuffelrock 12 (2 CD)/Disc 2`

### KuschelRock 11  (2)
    - `KuschelRock 11 [CD1]`
        - artist: KushelRock  ·  year: 1997
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 11 [2CD] (1997)/CD 1`
    - `KuschelRock 11 [CD2]`
        - artist: KushelRock  ·  year: 1997
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 11 [2CD] (1997)/CD 2`

### KuschelRock 12  (2)
    - `KuschelRock 12 [CD1]`
        - artist: KushelRock  ·  year: 1998
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 12 [2CD] (1998)/CD 1`
    - `KuschelRock 12 [CD2]`
        - artist: KushelRock  ·  year: 1998
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 12 [2CD] (1998)/CD 2`

### KuschelRock 14  (2)
    - `KuschelRock 14 [CD1]`
        - artist: KushelRock  ·  year: 2000
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 14 [2CD] (2000)/CD 1`
    - `KuschelRock 14 [CD2]`
        - artist: KushelRock  ·  year: 2000
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 14 [2CD] (2000)/CD 2`

### KuschelRock 16  (2)
    - `KuschelRock 16 [CD1]`
        - artist: KushelRock  ·  year: 2002
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 16 [2CD] (2002)/CD 1`
    - `KuschelRock 16 [CD2]`
        - artist: KushelRock  ·  year: 2002
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 16 [2CD] (2002)/CD 2`

### Kuschelrock 17  (2)
    - `Kuschelrock 17 [CD1]`
        - artist: KushelRock  ·  year: 2003
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 17 [2CD] (2003)/CD 1`
    - `Kuschelrock 17 [CD2]`
        - artist: KushelRock  ·  year: 2003
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 17 [2CD] (2003)/CD 2`

### KuschelRock 18  (2)
    - `KuschelRock 18 [CD1]`
        - artist: KushelRock  ·  year: 2004
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 18 [2CD] (2004)/CD 1`
    - `KuschelRock 18 [CD2]`
        - artist: KushelRock  ·  year: 2004
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 18 [2CD] (2004)/CD 2`

### KuschelRock 19  (2)
    - `KuschelRock 19 [CD1]`
        - artist: KushelRock  ·  year: 2005
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 19 [2CD] (2005)/CD 1`
    - `KuschelRock 19 [CD2]`
        - artist: KushelRock  ·  year: 2005
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 19 [2CD] (2005)/CD 2`

### KuschelRock 20  (2)
    - `KuschelRock 20 [CD1]`
        - artist: KushelRock  ·  year: 2006
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 20 [2CD] (2006)/CD 1`
    - `KuschelRock 20 [CD2]`
        - artist: KushelRock  ·  year: 2006
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 20 [2CD] (2006)/CD 2`

### KuschelRock 21  (2)
    - `KuschelRock 21 [CD1]`
        - artist: KushelRock  ·  year: 2007
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 21 [2CD] (2007)/CD 1`
    - `KuschelRock 21 [CD2]`
        - artist: KushelRock  ·  year: 2007
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 21 [2CD] (2007)/CD 2`

### KuschelRock 22  (2)
    - `KuschelRock 22 [CD1]`
        - artist: KushelRock  ·  year: 2008
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 22 [2CD] (2008)/CD 1`
    - `KuschelRock 22 [CD2]`
        - artist: KushelRock  ·  year: 2008
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 22 [2CD] (2008)/CD 2`

### KuschelRock 23  (2)
    - `KuschelRock 23 [CD1]`
        - artist: KushelRock  ·  year: 2009
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 23 [2CD] (2009)/CD 1`
    - `KuschelRock 23 [CD2]`
        - artist: KushelRock  ·  year: 2009
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 23 [2CD] (2009)/CD 2`

### KuschelRock 24  (2)
    - `KuschelRock 24 [CD2]`
        - artist: KushelRock  ·  year: 2010
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 24 [2CD] (2010)/CD 2`
    - `Kuschelrock 24 [CD1]`
        - artist: KushelRock  ·  year: 2010
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 24 [2CD] (2010)/CD 1`

### KuschelRock 25  (3)
    - `KuschelRock 25 [CD1]`
        - artist: KushelRock  ·  year: 2011
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 25 [3CD] (2011)/CD 1`
    - `KuschelRock 25 [CD2]`
        - artist: KushelRock  ·  year: 2011
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 25 [3CD] (2011)/CD 2`
    - `KuschelRock 25 [CD3]`
        - artist: KushelRock  ·  year: 2011
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 25 [3CD] (2011)/CD 3`

### KuschelRock 26  (3)
    - `KuschelRock 26 [CD1]`
        - artist: KushelRock  ·  year: 2012
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 26 [3CD] (2012)/CD 1`
    - `KuschelRock 26 [CD2]`
        - artist: KushelRock  ·  year: 2012
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 26 [3CD] (2012)/CD 2`
    - `KuschelRock 26 [CD3]`
        - artist: KushelRock  ·  year: 2012
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 26 [3CD] (2012)/CD 3`

### KuschelRock 27  (3)
    - `KuschelRock 27 [CD1]`
        - artist: KushelRock  ·  year: 2013
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 27 [3CD] (2013)/CD 1`
    - `KuschelRock 27 [CD2]`
        - artist: KushelRock  ·  year: 2013
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 27 [3CD] (2013)/CD 2`
    - `KuschelRock 27 [CD3]`
        - artist: KushelRock  ·  year: 2013
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 27 [3CD] (2013)/CD 3`

### Kuschelrock 28  (3)
    - `Kuschelrock 28 (CD1)`
        - artist: KushelRock  ·  year: 2014
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 28 [3CD] (2014)/CD 1`
    - `Kuschelrock 28 (CD2)`
        - artist: KushelRock  ·  year: 2014
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 28 [3CD] (2014)/CD 2`
    - `Kuschelrock 28 (CD3)`
        - artist: KushelRock  ·  year: 2014
        - folder: `D:/music/_cd_rip/01. VA - Kuschelrock vol.1-33  (63CDs, 1987-2019 ,CD,Germany) 16-44/VA - Kuschelrock Vol. 1-33 (1987-2019)/KuschelRock 28 [3CD] (2014)/CD 3`

### L'amour des amis au Japon  (4)
    - `L'amour des amis au Japon (Disc 1)`
        - artist: Paul Mauriat  ·  year: 2013
        - folder: `D:/music/Paul Mauriat (FLAC)/2013. L'amour des amis au Japon (4 CD)/Disc 1`
    - `L'amour des amis au Japon (Disc 2)`
        - artist: Paul Mauriat  ·  year: 2013
        - folder: `D:/music/Paul Mauriat (FLAC)/2013. L'amour des amis au Japon (4 CD)/Disc 2`
    - `L'amour des amis au Japon (Disc 3)`
        - artist: Paul Mauriat  ·  year: 2013
        - folder: `D:/music/Paul Mauriat (FLAC)/2013. L'amour des amis au Japon (4 CD)/Disc 3`
    - `L'amour des amis au Japon (Disc 4)`
        - artist: Paul Mauriat  ·  year: 2013
        - folder: `D:/music/Paul Mauriat (FLAC)/2013. L'amour des amis au Japon (4 CD)/Disc 4`

### L.A. Forum - Live In 1975  (2)
    - `L.A. Forum - Live In 1975 [disc 1]`
        - artist: The Rolling Stones  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2012 L.A. Friday (Live 1975) [Promotone EAGDV038]/Disc 1`
    - `L.A. Forum - Live In 1975 [disc 2]`
        - artist: The Rolling Stones  ·  year: 2014
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2012 L.A. Friday (Live 1975) [Promotone EAGDV038]/Disc 2`

### Ladies Of The Road: Live 1971-1972 [DGM0203]  (2)
    - `Ladies Of The Road: Live 1971-1972 [DGM0203] CD1`
        - artist: King Crimson  ·  year: 2002
        - folder: `D:/music/King Crimson/3.Live/2002. Ladies Of The Road/2002, US, Discipline Global Mobile, DGM0203/CD 1`
    - `Ladies Of The Road: Live 1971-1972 [DGM0203] CD2`
        - artist: King Crimson  ·  year: 2002
        - folder: `D:/music/King Crimson/3.Live/2002. Ladies Of The Road/2002, US, Discipline Global Mobile, DGM0203/CD 2`

### Larks' Tongues In Aspic [2012, DGM5011]  (2)
    - `Larks' Tongues In Aspic [2012, DGM5011] CD1`
        - artist: King Crimson  ·  year: 1973
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1973. Larks' Tongues In Aspic/2012, US, Discipline Global Mobile, DGM5011/CD 1`
    - `Larks' Tongues In Aspic [2012, DGM5011] CD2`
        - artist: King Crimson  ·  year: 1973
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1973. Larks' Tongues In Aspic/2012, US, Discipline Global Mobile, DGM5011/CD 2`

### Last Bleibt Last  (2)
    - `Last Bleibt Last - CD2`
        - artist: James Last  ·  year: 2000
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/Last bleibt Last [Polydor 65 309 7, Club Edition] 2CD/CD2`
    - `Last bleibt Last - CD1`
        - artist: James Last  ·  year: 2000
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/Last bleibt Last [Polydor 65 309 7, Club Edition] 2CD/CD1`

### Led Zeppelin  (4)
    - `Led Zeppelin (Disc 3)`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1990. Boxed Set (Atlantic 7 82144-2, USA)/Disc 3`
    - `Led Zeppelin (Disc I)`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1990. Boxed Set (Atlantic 7 82144-2, USA)/Disc 1`
    - `Led Zeppelin (Disc IV)`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1990. Boxed Set (Atlantic 7 82144-2, USA)/Disc 4`
    - `Led Zeppelin [Disc 2]`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1990. Boxed Set (Atlantic 7 82144-2, USA)/Disc 2`

### Les Mots (Edition Limitee)  (7)
    - `Les Mots (Edition Limitee) CD1`
        - artist: Mylene Farmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2001 - Les mots (Edition Limitee) (2CD)/Mylene Farmer - Les Mots (Edition Limitee) CD1`
    - `Les Mots (Edition Limitee) CD2`
        - artist: Mylene Farmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2001 - Les mots (Edition Limitee) (2CD)/Mylene Farmer - Les Mots (Edition Limitee) CD2`
    - `Les Mots - CD 2`
        - artist: Mylene Farmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2001 - Les mots (2CD)/Mylene Farmer - Les Mots CD2`
    - `Les Mots CD1`
        - artist: Mylene Farmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2001 - Les mots (2CD)/Mylene Farmer - Les mots CD1`
    - `Les mots (Long Box Edition Limitee) CD1`
        - artist: Mylene Farmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2001 - Les mots (Long Box Edition Limitee) (3CD+DVD)/CD1`
    - `Les mots (Long Box Edition Limitee) CD2`
        - artist: Mylene Farmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2001 - Les mots (Long Box Edition Limitee) (3CD+DVD)/CD2`
    - `Les mots (Long Box Edition Limitee) CD3`
        - artist: Mylene Farmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2001 - Les mots (Long Box Edition Limitee) (3CD+DVD)/CD3`

### Let Go (Limited Edition)  (2)
    - `Let Go (Limited Edition) CD1`
        - artist: Avril Lavigne  ·  year: 2003
        - folder: `D:/music/_cd_rip/Avril Lavigne/[.Albums.]/2003 - Let Go (Limited Edition) [82876-52937-2 Australia]/CD 1`
    - `Let Go (Limited Edition) CD2`
        - artist: Avril Lavigne  ·  year: 2003
        - folder: `D:/music/_cd_rip/Avril Lavigne/[.Albums.]/2003 - Let Go (Limited Edition) [82876-52937-2 Australia]/CD 2`

### Let It Be (MFSL SuperVinyl 24/96)  (3)
    - `Let It Be  (MFSL SuperVinyl 24/96)`
        - artist: The Beatles  ·  year: 1970
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/13. 1970 - Let It Be {24bit.96kHz Vinyl Rip MFSL 1-109}`
    - `Let It Be (Disc 1) (B0032261-02)`
        - artist: The Beatles  ·  year: 2021
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2021 - Let It Be/Let It Be 2CD Edition (Apple B0032261-02)/Disc 1`
    - `Let It Be (Disc 16)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - CD Singles Collection/16 Let It Be`

### Let It Be... Naked  (2)
    - `Let It Be... Naked (Disc 1)`
        - artist: The Beatles  ·  year: 2003
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/2003 - Let It Be... Naked/Let It Be... Naked (07243 595713 2 4)/Disc 1`
    - `Let It Be... Naked (Disc 1) (CDP 7243 5 95713 2 4)`
        - artist: The Beatles  ·  year: 2003
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/3 Compilations/2003 - Let It Be... Naked/Let It Be... Naked (CDP 7243 5 95713 2 4)/Disc 1`

### Lightbulb Sun (Special Edition)  (2)
    - `Lightbulb Sun (Special Edition) CD1`
        - artist: Porcupine Tree  ·  year: 2000
        - folder: `D:/music/Porcupine Tree Main discography/Original Albums/2001 - Lightbulb Sun 2CD (Special Germany Edition) (KScope, Snapper SMACD 841X UK)/CD1`
    - `Lightbulb Sun (Special Edition) CD2`
        - artist: Porcupine Tree  ·  year: 2001
        - folder: `D:/music/Porcupine Tree Main discography/Original Albums/2001 - Lightbulb Sun 2CD (Special Germany Edition) (KScope, Snapper SMACD 841X UK)/CD2`

### Live (Albert 472652 2)  (5)
    - `Live (Albert 472652 2, Disc 1)`
        - artist: AC/DC  ·  year: 1992
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Albums/1992 - Live/1992 Australia Albert 472652 2/Disc 1`
    - `Live (Albert 472652 2, Disc 2)`
        - artist: AC/DC  ·  year: 1992
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Albums/1992 - Live/1992 Australia Albert 472652 2/Disc 2`
    - `Live (CD single)`
        - artist: AC/DC  ·  year: 1992
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Singles & Promos/1992 - Live - 5 Titres Inedits En Concert (ATCO 7567-96065-2)`
    - `Live (Disc 1)`
        - artist: AC/DC, Uriah Heep  ·  year: 1992, 1973
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Albums/1992 - Live/1992 Germany ATCO 7567-92212-2/Disc 1`
        - folder: `D:/music/_cd_rip/Uriah Heep - Discography/Live/1973. Live January 1973/1973. Live January 1973 (Sanctuary Rec CMTDD 329, England)/Disc1`
    - `Live (Disc 2)`
        - artist: AC/DC, Uriah Heep  ·  year: 1992, 1973
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Albums/1992 - Live/1992 Germany ATCO 7567-92212-2/Disc 2`
        - folder: `D:/music/_cd_rip/Uriah Heep - Discography/Live/1973. Live January 1973/1973. Live January 1973 (Sanctuary Rec CMTDD 329, England)/Disc2`

### Live & Loud (1995, EPC 481676 2)  (6)
    - `Live & Loud (1995, EPC 481676 2, CD1)`
        - artist: Ozzy Osbourne  ·  year: 1993
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Live Albums/1993 Live & Loud/1993 Live & Loud [1995 Austria EPC 481676 2 Sony]/CD1`
    - `Live & Loud (1995, EPC 481676 2, CD2)`
        - artist: Ozzy Osbourne  ·  year: 1993
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Live Albums/1993 Live & Loud/1993 Live & Loud [1995 Austria EPC 481676 2 Sony]/CD2`
    - `Live & Loud (Disc 1) (Z2K 48973)`
        - artist: Ozzy Osbourne  ·  year: 1993
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Live Albums/1993 Live & Loud/1993 Live & Loud [1993 USA Z2K 48973 Epic]/CD 1`
    - `Live & Loud (Disc 2) (Z2K 48973)`
        - artist: Ozzy Osbourne  ·  year: 1993
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Live Albums/1993 Live & Loud/1993 Live & Loud [1993 USA Z2K 48973 Epic]/CD 2`
    - `Live & Loud (Japanese SRCS 6763, CD1)`
        - artist: Ozzy Osbourne  ·  year: 1993
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Live Albums/1993 Live & Loud/1993 Live & Loud [1993 Japan SRCS 6763~4 Sony]/CD1`
    - `Live & Loud (Japanese SRCS 6764, CD2)`
        - artist: Ozzy Osbourne  ·  year: 1993
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Live Albums/1993 Live & Loud/1993 Live & Loud [1993 Japan SRCS 6763~4 Sony]/CD2`

### Live 1966  (2)
    - `Live 1966 (CD1)`
        - artist: Bob Dylan  ·  year: 1966
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/1998. Bob Dylan - The Bootleg Series Vol.4 Live 1966 (Columbia-Legacy 88697732892, EU)/Disc1`
    - `Live 1966 (The Bootleg Series Vol. 4 - The Albert Hall Concert) CD 2`
        - artist: Bob Dylan  ·  year: 1998
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/1998. Bob Dylan - The Bootleg Series Vol.4 Live 1966 (Columbia-Legacy 88697732892, EU)/Disc2`

### Live 1983-1989  (3)
    - `Live 1983-1989 CD1`
        - artist: Eurythmics  ·  year: 1993
        - folder: `D:/music/Eurythmics - Live/CD1`
    - `Live 1983-1989 CD2`
        - artist: Eurythmics  ·  year: 1983
        - folder: `D:/music/Eurythmics - Live/CD2`
    - `Live 1983-1989 CD3`
        - artist: Eurythmics  ·  year: 1989
        - folder: `D:/music/Eurythmics - Live/CD3 (bonus)`

### Live After Death (7243 8 35873 2 2)  (4)
    - `Live After Death (7243 8 35873 2 2, CD1)`
        - artist: Iron Maiden  ·  year: 1985
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/1985 Live After Death/1985 Live After Death [1995 Holand 7243 8 35873 2 2 EMI]/CD 1`
    - `Live After Death (7243 8 35873 2 2, CD2)`
        - artist: Iron Maiden  ·  year: 1985
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/1985 Live After Death/1985 Live After Death [1995 Holand 7243 8 35873 2 2 EMI]/CD 2`
    - `Live After Death [Castle 107-2] CD 1`
        - artist: Iron Maiden  ·  year: 1985
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/1985 Live After Death/1985 Live After Death [1995 USA CASTLE 107-2. Castle Records]/CD 1`
    - `Live After Death [Castle 107-2] CD 2`
        - artist: Iron Maiden  ·  year: 1985
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/1985 Live After Death/1985 Live After Death [1995 USA CASTLE 107-2. Castle Records]/CD 2`

### Live At Brixton Academy (Germany, Steamhammer, SPV 089-72622 DCD)  (2)
    - `Live At Brixton Academy (Germany, Steamhammer, SPV 089-72622 DCD) (CD 1)`
        - artist: Motorhead  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2003-Live At Brixton Academy (2003, Germany, Steamhammer, SPV 089-72622 DCD, 2CD)/CD1`
    - `Live At Brixton Academy (Germany, Steamhammer, SPV 089-72622 DCD) (CD 2)`
        - artist: Motorhead  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2003-Live At Brixton Academy (2003, Germany, Steamhammer, SPV 089-72622 DCD, 2CD)/CD2`

### Live At Budokan  (3)
    - `Live At Budokan (Disc 1)`
        - artist: Dream Theater  ·  year: 2004
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2004-Live At Budokan`
    - `Live At Budokan (Disc 2)`
        - artist: Dream Theater  ·  year: 2004
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2004-Live At Budokan`
    - `Live At Budokan (Disc 3)`
        - artist: Dream Theater  ·  year: 2004
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2004-Live At Budokan`

### Live At Donington (Japan TOCP-8067)  (2)
    - `Live At Donington (Japan TOCP-8067, CD1)`
        - artist: Iron Maiden  ·  year: 1993
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/1993 Live At Donington/1993 Live At Donington [1993 Japan TOCP-8067~68 EMI]/CD 1`
    - `Live At Donington (Japan TOCP-8068, CD2)`
        - artist: Iron Maiden  ·  year: 1993
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/1993 Live At Donington/1993 Live At Donington [1993 Japan TOCP-8067~68 EMI]/CD 2`

### Live At Knebworth - 10th Anniversary Edition  (2)
    - `Live At Knebworth - 10th Anniversary Edition (CD1)`
        - artist: Robbie Williams  ·  year: 2013
        - folder: `D:/music/Robbie Williams/Audio/Albums/Live At Knebworth - 10th Anniversary Edition (2013)/CD1`
    - `Live At Knebworth - 10th Anniversary Edition (CD2)`
        - artist: Robbie Williams  ·  year: 2013
        - folder: `D:/music/Robbie Williams/Audio/Albums/Live At Knebworth - 10th Anniversary Edition (2013)/CD2`

### Live At Leeds - Deluxe Edition  (2)
    - `Live At Leeds - Deluxe Edition (Disc 1)`
        - artist: The Who  ·  year: 1970
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1970 - Live At Leeds (2 CD Deluxe Edition Polydor 112618-2)/Disc 1`
    - `Live At Leeds - Deluxe Edition (Disc 2)`
        - artist: The Who  ·  year: 1970
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1970 - Live At Leeds (2 CD Deluxe Edition Polydor 112618-2)/Disc 2`

### Live At Long Beach 1976 (2016, 2CD, 0210940EMU)  (2)
    - `Live At Long Beach 1976 CD1 (2016, 2CD, 0210940EMU)`
        - artist: Deep Purple  ·  year: 2016
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1995. Live At Long Beach 1976 (2016, 2CD, Edel, Germany, 0210940EMU)/CD1`
    - `Live At Long Beach 1976 CD2 (2016, 2CD, 0210940EMU)`
        - artist: Deep Purple  ·  year: 2016
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1995. Live At Long Beach 1976 (2016, 2CD, Edel, Germany, 0210940EMU)/CD2`

### Live At Luna Park  (3)
    - `Live At Luna Park CD 1`
        - artist: Dream Theater  ·  year: 2013
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2013-Live At Luna Park`
    - `Live At Luna Park CD 2`
        - artist: Dream Theater  ·  year: 2013
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2013-Live At Luna Park`
    - `Live At Luna Park CD 3`
        - artist: Dream Theater  ·  year: 2013
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2013-Live At Luna Park`

### Live At Montreux 1997 (2015, 2CD, EDGCD640)  (2)
    - `Live At Montreux 1997 CD1 (2015, 2CD, EDGCD640)`
        - artist: Emerson, Lake & Palmer  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2015. Emerson, Lake & Palmer - Live At Montreux 1997 (2015, 2CD, Eagle Records, Germany, EDGCD640)/CD1`
    - `Live At Montreux 1997 CD2 (2015, 2CD, EDGCD640)`
        - artist: Emerson, Lake & Palmer  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2015. Emerson, Lake & Palmer - Live At Montreux 1997 (2015, 2CD, Eagle Records, Germany, EDGCD640)/CD2`

### Live at Montreux 2011 (2011, 2CD, EDGCD470)  (2)
    - `Live at Montreux 2011 CD1 (2011, 2CD, EDGCD470)`
        - artist: Deep Purple  ·  year: 2011
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2011. With Orchestra - Live at Montreux 2011 (2011, 2CD, Eagle Rock, EU, Germany, EDGCD470)/CD1`
    - `Live at Montreux 2011 CD2 (2011, 2CD, EDGCD470)`
        - artist: Deep Purple  ·  year: 2011
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2011. With Orchestra - Live at Montreux 2011 (2011, 2CD, Eagle Rock, EU, Germany, EDGCD470)/CD2`

### Live At Nassau Coliseum '78 (VVNL23092)  (2)
    - `Live At Nassau Coliseum '78 CD1 (VVNL23092)`
        - artist: Emerson, Lake & Palmer  ·  year: 2011
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2011. Emerson, Lake & Palmer - Live  At Nassau Coliseum '78 (2012, 2CD, Shout! Factory, France & Benelux, VVNL23092)/CD1`
    - `Live At Nassau Coliseum '78 CD2 (VVNL23092)`
        - artist: Emerson, Lake & Palmer  ·  year: 2011
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2011. Emerson, Lake & Palmer - Live  At Nassau Coliseum '78 (2012, 2CD, Shout! Factory, France & Benelux, VVNL23092)/CD2`

### Live At River Plate  (2)
    - `Live At River Plate - Disc 1`
        - artist: AC/DC  ·  year: 2012
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Albums/2012 - Live at River Plate/CD1`
    - `Live At River Plate - Disc 2`
        - artist: AC/DC  ·  year: 2012
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Albums/2012 - Live at River Plate/CD2`

### Live At The BBC  (6)
    - `Live At The BBC (Disc 1)`
        - artist: The Beatles  ·  year: 1994
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/1994 - Live At The BBC/Live At The BBC (EMI 7243 8 31796 2 6)/Disc 1`
    - `Live At The BBC (Disc 1) (CDP 7243 8 31796 2 6)`
        - artist: The Beatles  ·  year: 1994
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/1994 - Live At The BBC/Live At The BBC (CDP 7243 8 31796 2 6)/Disc 1`
    - `Live At The BBC (Disc 2)`
        - artist: The Beatles  ·  year: 1994
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/1994 - Live At The BBC/Live At The BBC (EMI 7243 8 31796 2 6)/Disc 2`
    - `Live At The BBC (Disc 2) (CDP 7243 8 31796 2 6)`
        - artist: The Beatles  ·  year: 1994
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/1994 - Live At The BBC/Live At The BBC (CDP 7243 8 31796 2 6)/Disc 2`
    - `Live At The BBC CD1 (Remaster 2013)`
        - artist: The Beatles  ·  year: 1994
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/1994 - Live At The BBC/Live At The BBC (Apple 3749153)/Disc 1`
    - `Live At The BBC CD2 (Remaster 2013)`
        - artist: The Beatles  ·  year: 1994
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/1994 - Live At The BBC/Live At The BBC (Apple 3749153)/Disc 2`

### Live At The Isle Of Wight Festival 1970  (2)
    - `Live At The Isle Of Wight Festival 1970 (Disc 1)`
        - artist: The Who  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1970 - Live At The Isle Of Wight Festival 1970 (2 CD Castle Communications EDF CD 326)/Disc 1`
    - `Live At The Isle Of Wight Festival 1970 (Disc 2)`
        - artist: The Who  ·  year: 1970
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1970 - Live At The Isle Of Wight Festival 1970 (2 CD Castle Communications EDF CD 326)/Disc 2`

### Live At The Nec (2006, 4CD, 82876759042)  (2)
    - `Live At The Nec CD1 (2006, 4CD, 82876759042)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Europe, 1993 (2006, 4CD, SONY BMG, EU, Germany, 82876759042)/2. Live AT The NEC 1993/CD1`
    - `Live At The Nec CD2 (2006, 4CD, 82876759042)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Europe, 1993 (2006, 4CD, SONY BMG, EU, Germany, 82876759042)/2. Live AT The NEC 1993/CD2`

### Live At The Olympia '96 (1997, 2CD, 724385798221)  (4)
    - `Live At The Olympia '96 CD1 (1997, 2CD, 724385798221)`
        - artist: Deep Purple  ·  year: 1996
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1997. Live At The Olympia '96 (1997, 2CD, Eagle, Holland-Germany, 724385798221)/CD1`
    - `Live At The Olympia '96 CD1 (1997, 2CD, TECW-35568-69)`
        - artist: Deep Purple  ·  year: 1997
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1997. Live At The Olympia '96 (1997, 2CD, Teichiku Records, Japan, TECW-35568-69)/CD1`
    - `Live At The Olympia '96 CD2 (1997, 2CD, 724385798221)`
        - artist: Deep Purple  ·  year: 1997
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1997. Live At The Olympia '96 (1997, 2CD, Eagle, Holland-Germany, 724385798221)/CD2`
    - `Live At The Olympia '96 CD2 (1997, 2CD, TECW-35568-69)`
        - artist: Deep Purple  ·  year: 1997
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1997. Live At The Olympia '96 (1997, 2CD, Teichiku Records, Japan, TECW-35568-69)/CD2`

### LIVE AT THE ROYAL ALBERT HALL  (2)
    - `LIVE AT THE ROYAL ALBERT HALL (DISC 1)`
        - artist: THE WHO  ·  year: 2003
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 2003 - Live At The Royal Albert Hall (3 CD SPV 093-74882)/Disc 1 (2000-11-27)`
    - `LIVE AT THE ROYAL ALBERT HALL (DISC 2)`
        - artist: THE WHO  ·  year: 2003
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 2003 - Live At The Royal Albert Hall (3 CD SPV 093-74882)/Disc 2 (2000-11-27)`

### Live at the Tokyo Dome  (2)
    - `Live at the Tokyo Dome (CD1)`
        - artist: The Rolling Stones  ·  year: 1990
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2015 Live At The Tokyo Dome [Eagle EAGDV051]/Disc 1`
    - `Live at the Tokyo Dome (CD2)`
        - artist: The Rolling Stones  ·  year: 1990
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2015 Live At The Tokyo Dome [Eagle EAGDV051]/Disc 2`

### Live Blood (EDGCD474)  (2)
    - `Live Blood (EDGCD474) CD1`
        - artist: Peter Gabriel  ·  year: 2012
        - folder: `D:/music/PETER GABRIEL - DISCOGRAPHY/(2012) Live Blood (EDGCD474)/CD1`
    - `Live Blood (EDGCD474) CD2`
        - artist: Peter Gabriel  ·  year: 2012
        - folder: `D:/music/PETER GABRIEL - DISCOGRAPHY/(2012) Live Blood (EDGCD474)/CD2`

### Live Denmark 1972 (2007, 2CD, PUR253)  (2)
    - `Live Denmark 1972 CD1 (2007, 2CD, PUR253)`
        - artist: Deep Purple  ·  year: 2002
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2002. Live In Denmark 1972 (2007, 2CD, Sonic Zoom Records, EU, Germany, PUR253)/CD1`
    - `Live Denmark 1972 CD2 (2007, 2CD, PUR253)`
        - artist: Deep Purple  ·  year: 2002
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2002. Live In Denmark 1972 (2007, 2CD, Sonic Zoom Records, EU, Germany, PUR253)/CD2`

### Live Encounters... (2009, 2CD, MASSCD0999DD)  (2)
    - `Live Encounters... CD1 (2009, 2CD, MASSCD0999DD)`
        - artist: Deep Purple  ·  year: 2004
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2004. Live Encounters... (2009, 2CD, Metal Mind, Poland, MASSCD0999DD)/CD1`
    - `Live Encounters... CD2 (2009, 2CD, MASSCD0999DD)`
        - artist: Deep Purple  ·  year: 2004
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2004. Live Encounters... (2009, 2CD, Metal Mind, Poland, MASSCD0999DD)/CD2`

### Live Era '87-'93  (4)
    - `Live Era '87-'93 (CD 2)`
        - artist: Guns N' Roses  ·  year: 1999
        - folder: `D:/music/Guns N' Roses/CD 2`
    - `Live Era '87-'93 (Disc 1)`
        - artist: Guns N' Roses  ·  year: 1999
        - folder: `D:/music/Guns n`Roses/1999 - Guns N' Roses - Live Era '87-'93/Disc 1 of 2`
    - `Live Era '87-'93 (Disc 2)`
        - artist: Guns N' Roses  ·  year: 1999
        - folder: `D:/music/Guns n`Roses/1999 - Guns N' Roses - Live Era '87-'93/Disc 2 of 2`
    - `Live Era '87-'93 - CD 1`
        - artist: Guns N' Roses  ·  year: 1999
        - folder: `D:/music/Guns N' Roses/CD 1`

### Live Evil (Deluxe Expanded Edition)  (6)
    - `Live Evil (Deluxe Expanded Edition, CD1)`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1982 Live Evil/1982 Live Evil [2010 Germany 2733929 Sanctuary]/CD1`
    - `Live Evil (Deluxe Expanded Edition, CD2)`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1982 Live Evil/1982 Live Evil [2010 Germany 2733929 Sanctuary]/CD2`
    - `Live Evil (Disc 1) (Remastered, Japan, TECW 350189)`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1982 Live Evil/1982 Live Evil [1996 Japan TECW-350189~350190 Teichiku]/CD 1`
    - `Live Evil (Disc 2) (Remastered, Japan, TECW 350190)`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1982 Live Evil/1982 Live Evil [1996 Japan TECW-350189~350190 Teichiku]/CD 2`
    - `Live Evil CD1 (Warner Bros. 9 23742-2)`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1982 Live Evil/1982 Live Evil [1989 USA 9 23742-2 Warner]/CD1`
    - `Live Evil CD2 (Warner Bros. 9 23742-2)`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1982 Live Evil/1982 Live Evil [1989 USA 9 23742-2 Warner]/CD2`

### Live Evil - The Rules Of Hell 2008  (2)
    - `Live Evil Disk 1 - The Rules Of Hell 2008`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2008 The Rules Of Hell 5 CD [USA R2 460156]/1982 Live Evil 2CD/CD 1`
    - `Live Evil Disk 2 - The Rules Of Hell 2008`
        - artist: Black Sabbath  ·  year: 1982
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2008 The Rules Of Hell 5 CD [USA R2 460156]/1982 Live Evil 2CD/CD 2`

### Live form Russia SPV DCD 089-5742 2  (2)
    - `Live form Russia (Disc 1) SPV DCD 089-5742 2`
        - artist: U.D.O.  ·  year: 2001
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2001 Live From Russia/2001 Live From Russia [2001 SPV DCD 089-5742 2 Germany]/CD 1`
    - `Live form Russia (Disc 2) SPV DCD 089-5742 2`
        - artist: U.D.O.  ·  year: 2001
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2001 Live From Russia/2001 Live From Russia [2001 SPV DCD 089-5742 2 Germany]/CD 2`

### Live From Moscow  (2)
    - `Live From Moscow CD1`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/Live/2020. Live From Moscow 1979/2020. Elton John - Live From Moscow 1979 (Rocket 00602577889424, EU)/CD1`
    - `Live From Moscow CD2`
        - artist: Elton John  ·  year: 2020
        - folder: `D:/music/_cd_rip/Sir Elton John/Live/2020. Live From Moscow 1979/2020. Elton John - Live From Moscow 1979 (Rocket 00602577889424, EU)/CD2`

### Live From Toronto  (2)
    - `Live From Toronto (Disc 1)`
        - artist: The Who  ·  year: 2006
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1982 - Live From Toronto (2 CD IMC Music IMA 104201)/Disc 1`
    - `Live From Toronto (Disc 2)`
        - artist: The Who  ·  year: 2006
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1982 - Live From Toronto (2 CD IMC Music IMA 104201)/Disc 2`

### Live In Birmingham 1993. NEC 1993 (2013, 2CD, HNECD025D)  (2)
    - `Live In Birmingham 1993. NEC 1993 CD1 (2013, 2CD, HNECD025D)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Birmingham 1993. 20th Anniversary (2013, 2CD, Hear No Evil, EC, France, HNECD025D)/CD1`
    - `Live In Birmingham 1993. NEC 1993 CD2 (2013, 2CD, HNECD025D)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Birmingham 1993. 20th Anniversary (2013, 2CD, Hear No Evil, EC, France, HNECD025D)/CD2`

### Live in Boston 1988 (2015, 2CD, ROC-CD-3326)  (2)
    - `Live in Boston 1988 CD1 (2015, 2CD, ROC-CD-3326)`
        - artist: 3  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Collaboration/2015. 3 - Live in Boston 1988 (2015, 2CD, RockBeat Records, USA, ROC-CD-3326)/CD1`
    - `Live in Boston 1988 CD2 (2015, 2CD, ROC-CD-3326)`
        - artist: 3  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Collaboration/2015. 3 - Live in Boston 1988 (2015, 2CD, RockBeat Records, USA, ROC-CD-3326)/CD2`

### Live In Brighton, 1971 [IECP-20005~10]  (2)
    - `Live In Brighton, 1971 [IECP-20005~10] CD1`
        - artist: King Crimson  ·  year: 2006
        - folder: `D:/music/King Crimson/5.Box, compilations/2006. The Collectors' King Crimson Volume Ten/2006, Japan, WHD Entertainment, IECP-20005~10/CD 4-5/CD 1`
    - `Live In Brighton, 1971 [IECP-20005~10] CD2`
        - artist: King Crimson  ·  year: 2006
        - folder: `D:/music/King Crimson/5.Box, compilations/2006. The Collectors' King Crimson Volume Ten/2006, Japan, WHD Entertainment, IECP-20005~10/CD 4-5/CD 2`

### Live in Bulgaria 2020 - Pandemic Survival Show (AFM 789-0)  (2)
    - `Live in Bulgaria 2020 - Pandemic Survival Show CD1 (AFM 789-0)`
        - artist: U.D.O.  ·  year: 2021
        - folder: `D:/music/U.D.O. - 2021 - Live In Bulgaria 2020 - Pandemic Survival Show [2CD-FLAC]`
    - `Live in Bulgaria 2020 - Pandemic Survival Show CD2 (AFM 789-0)`
        - artist: U.D.O.  ·  year: 2021
        - folder: `D:/music/U.D.O. - 2021 - Live In Bulgaria 2020 - Pandemic Survival Show [2CD-FLAC]`

### Live In California Long Beach Arena 1976 (1995, 2CD, DPVSOP CD217)  (2)
    - `Live In California Long Beach Arena 1976 CD1 (1995, 2CD, DPVSOP CD217)`
        - artist: Deep Purple  ·  year: 1995
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1995. Live In California 1976. Long Beach Arena 1976 (1995, 2CD, Connoisseur, UK, DPVSOP CD217)/CD1`
    - `Live In California Long Beach Arena 1976 CD2 (1995, 2CD, DPVSOP CD217)`
        - artist: Deep Purple  ·  year: 2005
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1995. Live In California 1976. Long Beach Arena 1976 (1995, 2CD, Connoisseur, UK, DPVSOP CD217)/CD2`

### Live In Gdansk  (3)
    - `Live In Gdansk (Disc 5)`
        - artist: David Gilmour  ·  year: 2008
        - folder: `D:/music/Pink Floyd - Solo Albums (lossless)/David Gilmour/2008 - Live In Gdansk/Disc-3`
    - `Live in Gdansk (CD1)`
        - artist: David Gilmour  ·  year: 2008
        - folder: `D:/music/Pink Floyd - Solo Albums (lossless)/David Gilmour/2008 - Live In Gdansk/Disc-1`
    - `Live in Gdansk (CD2)`
        - artist: David Gilmour  ·  year: 2008
        - folder: `D:/music/Pink Floyd - Solo Albums (lossless)/David Gilmour/2008 - Live In Gdansk/Disc-2`

### Live In Japan  (5)
    - `Live In Japan (Disc 1)`
        - artist: George Harrison  ·  year: 2004
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/05 Box Sets/2004 The Dark Horse Years 1976-1992 (Capitol CDP 7243 5 97051 0 1)/1992 Live In Japan/Disc 1`
    - `Live In Japan (Disc 2)`
        - artist: George Harrison  ·  year: 2004
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/05 Box Sets/2004 The Dark Horse Years 1976-1992 (Capitol CDP 7243 5 97051 0 1)/1992 Live In Japan/Disc 2`
    - `Live In Japan CD1 (1993, 3CD, 724382772620)`
        - artist: Deep Purple  ·  year: 1993
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1972. Live In Japan (1993, 3CD, EMI, Holland-Poland, 724382772620)/CD1`
    - `Live In Japan CD2 (1993, 3CD, 724382772620)`
        - artist: Deep Purple  ·  year: 1993
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1972. Live In Japan (1993, 3CD, EMI, Holland-Poland, 724382772620)/CD2`
    - `Live In Japan CD3 (1993, 3CD, 724382772620)`
        - artist: Deep Purple  ·  year: 1993
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1972. Live In Japan (1993, 3CD, EMI, Holland-Poland, 724382772620)/CD3`

### Live in Leeds 1982  (2)
    - `Live in Leeds 1982 (CD 1)`
        - artist: The Rolling Stones  ·  year: 2015
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2015 Roundhay Park, Live In Leeds 1982 [Eagle EAGDV053]/Disc 1`
    - `Live in Leeds 1982 (CD 2)`
        - artist: The Rolling Stones  ·  year: 2015
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2015 Roundhay Park, Live In Leeds 1982 [Eagle EAGDV053]/Disc 2`

### Live In London (2003, SPV, SPV 092-74262 DCD-E, Germany)  (10)
    - `Live In London (2003, SPV, SPV 092-74262 DCD-E, Germany, CD 1)`
        - artist: Judas Priest  ·  year: 2003
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/2003 - Live In London/2003 - Live In London [SPV, SPV 092-72262 DCD-E, Germany]`
    - `Live In London (2003, SPV, SPV 092-74262 DCD-E, Germany, CD 2)`
        - artist: Judas Priest  ·  year: 2003
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/2003 - Live In London/2003 - Live In London [SPV, SPV 092-72262 DCD-E, Germany]`
    - `Live In London (2003, Victor, VICP-62158, Japan, CD 1)`
        - artist: Judas Priest  ·  year: 2003
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/2003 - Live In London/2003 - Live In London [Victor, VICP-62158~9, Japan]`
    - `Live In London (2003, Victor, VICP-62159, Japan, CD 2)`
        - artist: Judas Priest  ·  year: 2003
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/2003 - Live In London/2003 - Live In London [Victor, VICP-62158~9, Japan]`
    - `Live In London - CD 1`
        - artist: James Last  ·  year: 1978
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/1978 - Live In London [Polydor 843 809-2] 2CD/CD1`
    - `Live In London - CD 2`
        - artist: James Last  ·  year: 1978
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/1978 - Live In London [Polydor 843 809-2] 2CD/CD2`
    - `Live In London CD1 (2007, 2CD, 5099950358021)`
        - artist: Deep Purple  ·  year: 1982
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1982. Live In London (2007, 2CD, EMI, EU, Poland, 5099950358021)/CD1`
    - `Live in London (CD1)`
        - artist: Leonard Cohen  ·  year: 2008
        - folder: `D:/music/_cd_rip/Leonard Cohen - Discography/17. Leonard Cohen - Live In London - 2009 {Columbia 88697405022}/CD1`
    - `Live in London (CD2)`
        - artist: Leonard Cohen  ·  year: 2009
        - folder: `D:/music/_cd_rip/Leonard Cohen - Discography/17. Leonard Cohen - Live In London - 2009 {Columbia 88697405022}/CD2`
    - `Live in London CD2 (2007, 2CD, 5099950358021)`
        - artist: Deep Purple  ·  year: 1982
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1982. Live In London (2007, 2CD, EMI, EU, Poland, 5099950358021)/CD2`

### Live In Montreal 1977 (2013, 2CD, 826663-14432)  (2)
    - `Live In Montreal 1977 CD1 (2013, 2CD, 826663-14432)`
        - artist: Emerson, Lake & Palmer  ·  year: 2013
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2013. Emerson, Lake & Palmer - Live In Montreal 1977 (2013, 2CD, Shout! Factory, USA, 826663-14432)/CD1`
    - `Live In Montreal 1977 CD2 (2013, 2CD, 826663-14432)`
        - artist: Emerson, Lake & Palmer  ·  year: 2013
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2013. Emerson, Lake & Palmer - Live In Montreal 1977 (2013, 2CD, Shout! Factory, USA, 826663-14432)/CD2`

### Live In Montreux 1969 (2006, 2CD, PUR257)  (2)
    - `Live In Montreux 1969 CD1 (2006, 2CD, PUR257)`
        - artist: Deep Purple  ·  year: 2003
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Montreux 1969 (2006, 2CD, Sonic Zoom Records, EU, Germany, PUR257)/CD1`
    - `Live In Montreux 1969 CD2 (2006, 2CD, PUR257)`
        - artist: Deep Purple  ·  year: 2003
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Montreux 1969 (2006, 2CD, Sonic Zoom Records, EU, Germany, PUR257)/CD2`

### Live In Munich 1977  (2)
    - `Live In Munich 1977 - Disc 1`
        - artist: Rainbow  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rainbow Discography/Live/1977. Rainbow - Live In Munich/1977. Rainbow - Live In Munich (Eagle Rec. EDGCD315, Germany)/disc1`
    - `Live In Munich 1977 - Disc 2`
        - artist: Rainbow  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rainbow Discography/Live/1977. Rainbow - Live In Munich/1977. Rainbow - Live In Munich (Eagle Rec. EDGCD315, Germany)/disc2`

### Live In Munich 1977 - (2013)  (2)
    - `Live In Munich 1977 - Disc 1 (2013)`
        - artist: Rainbow  ·  year: 2013
        - folder: `D:/music/_cd_rip/Rainbow Discography/Live/1977. Rainbow - Live In Munich/1977. Rainbow - Live In Munich (Eagle Rec. EDGCD503, Germany)/disc1`
    - `Live In Munich 1977 - Disc 2 (2013)`
        - artist: Rainbow  ·  year: 2013
        - folder: `D:/music/_cd_rip/Rainbow Discography/Live/1977. Rainbow - Live In Munich/1977. Rainbow - Live In Munich (Eagle Rec. EDGCD503, Germany)/disc2`

### Live in Sofia  (2)
    - `Live in Sofia CD1`
        - artist: U.D.O.  ·  year: 2012
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2012 Live In Sofia/2012 Live In Sofia [2012 AFM 410-7 Germany]/CD 1`
    - `Live in Sofia CD2`
        - artist: U.D.O.  ·  year: 2012
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2012 Live In Sofia/2012 Live In Sofia [2012 AFM 410-7 Germany]/CD 2`

### Live In Stockholm (2006, 2CD, PUR338D)  (2)
    - `Live In Stockholm CD1 (2006, 2CD, PUR338D)`
        - artist: Deep Purple  ·  year: 1988
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2005. Live In Stockholm (2006, 2CD, Darker Than Blue Ltd., EU, Germany, PUR338D)/CD1`
    - `Live In Stockholm CD2 (2006, 2CD, PUR338D)`
        - artist: Deep Purple  ·  year: 2005
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2005. Live In Stockholm (2006, 2CD, Darker Than Blue Ltd., EU, Germany, PUR338D)/CD2`

### Live In Stockholm 1970 (2014, 2CD+DVD, 0208677ERE)  (2)
    - `Live In Stockholm 1970 CD1 (2014, 2CD+DVD, 0208677ERE)`
        - artist: Deep Purple  ·  year: 2014
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2014. Live In Stockholm 1970 (2014, 2CD+DVD, Edel, Germany, 0208677ERE)/CD1`
    - `Live In Stockholm 1970 CD2 (2014, 2CD+DVD, 0208677ERE)`
        - artist: Deep Purple  ·  year: 2014
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2014. Live In Stockholm 1970 (2014, 2CD+DVD, Edel, Germany, 0208677ERE)/CD2`

### Live in Stuttgart (2006, 4CD, 82876759042)  (2)
    - `Live in Stuttgart CD1 (2006, 4CD, 82876759042)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Europe, 1993 (2006, 4CD, SONY BMG, EU, Germany, 82876759042)/1. Live In Stuttgart 1993/CD1`
    - `Live in Stuttgart CD2 (2006, 4CD, 82876759042)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Europe, 1993 (2006, 4CD, SONY BMG, EU, Germany, 82876759042)/1. Live In Stuttgart 1993/CD2`

### Live In Stuttgart 1993. Stuttgart 1993 (2013, 2CD, HNECD024D)  (2)
    - `Live In Stuttgart 1993. Stuttgart 1993 CD1 (2013, 2CD, HNECD024D)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Stuttgart 1993. 20th Anniversary (2013, 2CD, Hear No Evil, EC, France, HNECD024D)/CD1`
    - `Live In Stuttgart 1993. Stuttgart 1993 CD2 (2013, 2CD, HNECD024D)`
        - artist: Deep Purple  ·  year: 2006
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2006. Live In Stuttgart 1993. 20th Anniversary (2013, 2CD, Hear No Evil, EC, France, HNECD024D)/CD2`

### Live In Toronto [DGM5013]  (2)
    - `Live In Toronto [DGM5013] CD1`
        - artist: King Crimson  ·  year: 2016
        - folder: `D:/music/King Crimson/3.Live/2016. Live In Toronto/2016, EU, Discipline Global Mobile, DGM5013/CD 1`
    - `Live In Toronto [DGM5013] CD2`
        - artist: King Crimson  ·  year: 2016
        - folder: `D:/music/King Crimson/3.Live/2016. Live In Toronto/2016, EU, Discipline Global Mobile, DGM5013/CD 2`

### Live in Verona (2014, VQCD-10406/7)  (2)
    - `Live in Verona CD1 (2014, VQCD-10406/7)`
        - artist: Deep Purple  ·  year: 2014
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2014. With Orchestra - Live in Verona (2014, 2CD, Ward, Japan, VQCD-10406-7)/CD1`
    - `Live in Verona CD2 (2014, VQCD-10406/7)`
        - artist: Deep Purple  ·  year: 2014
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2014. With Orchestra - Live in Verona (2014, 2CD, Ward, Japan, VQCD-10406-7)/CD2`

### Live In Warsaw, 2000 [IECP-20005~10]  (2)
    - `Live In Warsaw, 2000 [IECP-20005~10] CD1`
        - artist: King Crimson  ·  year: 2006
        - folder: `D:/music/King Crimson/5.Box, compilations/2006. The Collectors' King Crimson Volume Ten/2006, Japan, WHD Entertainment, IECP-20005~10/CD 1-2/CD 1`
    - `Live In Warsaw, 2000 [IECP-20005~10] CD2`
        - artist: King Crimson  ·  year: 2006
        - folder: `D:/music/King Crimson/5.Box, compilations/2006. The Collectors' King Crimson Volume Ten/2006, Japan, WHD Entertainment, IECP-20005~10/CD 1-2/CD 2`

### Live Licks  (4)
    - `Live Licks (CD 1)`
        - artist: The Rolling Stones  ·  year: 2004
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2004 Live Licks/Live Licks [Virgin 07243 875186 2 9]/Disc 1`
    - `Live Licks (CD 2)`
        - artist: The Rolling Stones  ·  year: 2004
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2004 Live Licks/Live Licks [Virgin 07243 875186 2 9]/Disc 2`
    - `Live Licks CD1 [VJCP-68700]`
        - artist: The Rolling Stones  ·  year: 2004
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2004 Live Licks/Live Licks [VJCP-68700-01]/Disc 1`
    - `Live Licks CD2 [VJCP-68701]`
        - artist: The Rolling Stones  ·  year: 2004
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2004 Live Licks/Live Licks [VJCP-68700-01]/Disc 2`

### Live on Air 65-68  (2)
    - `Live on Air 65-68 - CD1`
        - artist: Tom Jones  ·  year: 2020
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2020 - Live on Air 1965-1968/CD1`
    - `Live on Air 65-68 - CD2`
        - artist: Tom Jones  ·  year: 2020
        - folder: `D:/music/_cd_rip/Tom Jones lossless/2020 - Live on Air 1965-1968/CD2`

### Live Shit: Binge & Purge  (3)
    - `Live Shit: Binge & Purge (Disc 1)`
        - artist: Metallica  ·  year: 1993
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1993 - Live Shit - Binge & Purge (3 CD)/1993 USA Elektra 61594-2/Disc 1`
    - `Live Shit: Binge & Purge (Disc 2)`
        - artist: Metallica  ·  year: 1993
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1993 - Live Shit - Binge & Purge (3 CD)/1993 USA Elektra 61594-2/Disc 2`
    - `Live Shit: Binge & Purge (Disc 3)`
        - artist: Metallica  ·  year: 1993
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1993 - Live Shit - Binge & Purge (3 CD)/1993 USA Elektra 61594-2/Disc 3`

### Live Your Life Be Free (Remastered & Expanded Special Edition)  (2)
    - `Live Your Life Be Free (Remastered & Expanded Special Edition) CD 1`
        - artist: Belinda Carlisle  ·  year: 1991
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1991 Live Your Life Be Free (2013, Edsel Records, EDSG 8027)/CD1`
    - `Live Your Life Be Free (Remastered & Expanded Special Edition) CD 2`
        - artist: Belinda Carlisle  ·  year: 1991
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1991 Live Your Life Be Free (2013, Edsel Records, EDSG 8027)/CD2`

### Live А Bercy  (2)
    - `Live А Bercy CD2`
        - artist: Mylene Farmer  ·  year: 1997
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/1997 - Live а Bercy (2CD)/Mylene Farmer - Live А Bercy CD2`
    - `Live а Bercy CD1`
        - artist: Mylene Farmer  ·  year: 1997
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/1997 - Live а Bercy (2CD)/Mylene Farmer - Live а Bercy CD1`

### Live: Enterainment Or Death  (2)
    - `Live: Enterainment Or Death - CD1`
        - artist: Motley Crue  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/1999 - Live Entertainment Or Death [2003, Masters 2000, 038 663-2, Germany]`
    - `Live: Enterainment Or Death - CD2`
        - artist: Motley Crue  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/1999 - Live Entertainment Or Death [2003, Masters 2000, 038 663-2, Germany]`

### Live: Take The Crown Stadium Tour 2013. 17.07.2013 Krieau Vienna  (3)
    - `Live: Take The Crown Stadium Tour 2013. 17.07.2013 Krieau Vienna (CD1)`
        - artist: Robbie Williams  ·  year: 2013
        - folder: `D:/music/Robbie Williams/Audio/Albums/Take The Crown (Live at Vienna) (2013)/CD1`
    - `Live: Take The Crown Stadium Tour 2013. 17.07.2013 Krieau Vienna (CD2)`
        - artist: Robbie Williams  ·  year: 2013
        - folder: `D:/music/Robbie Williams/Audio/Albums/Take The Crown (Live at Vienna) (2013)/CD2`
    - `Live: Take The Crown Stadium Tour 2013. 17.07.2013 Krieau Vienna (CD3)`
        - artist: Robbie Williams  ·  year: 2013
        - folder: `D:/music/Robbie Williams/Audio/Albums/Take The Crown (Live at Vienna) (2013)/CD3`

### Loud As F@*k  (2)
    - `Loud As F@*k (DISC 1)`
        - artist: MOTLEY CRUE  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2003 - Loud As F@k [Motley Rec., 0602498104699, Germany]`
    - `Loud As F@*k (DISC 2)`
        - artist: MOTLEY CRUE  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2003 - Loud As F@k [Motley Rec., 0602498104699, Germany]`

### Love Is Blue (40th Anniversary Edition)  (2)
    - `Love Is Blue (40th Anniversary Edition) [Disc 1]`
        - artist: Paul Mauriat  ·  year: 2008
        - folder: `D:/music/Paul Mauriat (FLAC)/2008. Love Is Blue - 40th Anniversary (2 CD)/Disc 1`
    - `Love Is Blue (40th Anniversary Edition) [Disc 2]`
        - artist: Paul Mauriat  ·  year: 2008
        - folder: `D:/music/Paul Mauriat (FLAC)/2008. Love Is Blue - 40th Anniversary (2 CD)/Disc 2`

### Love You Live [CBS]  (8)
    - `Love You Live (Disc 1) [CBS]`
        - artist: The Rolling Stones  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/5 Box Sets/1990 Collection 1971-1989 [CBS 4669182]/1977 Love You Live [CBS 450208 2]/Disc 1`
    - `Love You Live (Disc 1) [CDV 2857]`
        - artist: The Rolling Stones  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/1977 Love You Live/Love You Live [Virgin CDV 2857]/Disc 1`
    - `Love You Live (Disc 2) [CBS]`
        - artist: The Rolling Stones  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/5 Box Sets/1990 Collection 1971-1989 [CBS 4669182]/1977 Love You Live [CBS 450208 2]/Disc 2`
    - `Love You Live (Disc 2) [CDV 2857]`
        - artist: The Rolling Stones  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/1977 Love You Live/Love You Live [Virgin CDV 2857]/Disc 2`
    - `Love You Live (Disc Two)`
        - artist: The Rolling Stones  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/1977 Love You Live/Love You Live [Polydor 0602527164243]/Disc 2`
    - `Love You Live CD1 (1986, 50DP 606-7)`
        - artist: The Rolling Stones  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/1977 Love You Live/Love You Live [CBS 50DP 606-7]/Disc 1`
    - `Love You Live CD2 (1986, 50DP 606-7)`
        - artist: The Rolling Stones  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/1977 Love You Live/Love You Live [CBS 50DP 606-7]/Disc 2`
    - `Love You Live [Disc 1]`
        - artist: Rolling Stones, The  ·  year: 1977
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/1977 Love You Live/Love You Live [Polydor 0602527164243]/Disc 1`

### Lulu [SHM-CD, UICR-1093]  (2)
    - `Lulu (Disc 1) [SHM-CD, UICR-1093]`
        - artist: Lou Reed & Metallica  ·  year: 2011
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/2011 - Lou Reed & Metallica - Lulu (2 CD)/2011 Japan SHM-CD, UICR-1093~4/Disc 1`
    - `Lulu (Disc 2) [SHM-CD, UICR-1094]`
        - artist: Lou Reed & Metallica  ·  year: 2011
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/2011 - Lou Reed & Metallica - Lulu (2 CD)/2011 Japan SHM-CD, UICR-1093~4/Disc 2`

### Made in Japan (1998, 2CD, 724385786426)  (2)
    - `Made in Japan CD1 (1998, 2CD, 724385786426)`
        - artist: Deep Purple  ·  year: 1972
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1972. Made in Japan (1998, 2CD, EMI, UK-Poland, 724385786426)/CD1`
    - `Made in Japan CD2 (1998, 2CD, 724385786426)`
        - artist: Deep Purple  ·  year: 1998
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1972. Made in Japan (1998, 2CD, EMI, UK-Poland, 724385786426)/CD2`

### Made in Japan 25th Anniversary (1998, 2CD, 724349419025)  (2)
    - `Made in Japan 25th Anniversary CD1 (1998, 2CD, 724349419025)`
        - artist: Deep Purple  ·  year: 1972
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1972. Made in Japan. 25th Anniversary (1998, 2CD, EMI, Holland-Germany, 724349419025)/CD1`
    - `Made in Japan 25th Anniversary CD2 (1998, 2CD, 724349419025)`
        - artist: Deep Purple  ·  year: 1998
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1972. Made in Japan. 25th Anniversary (1998, 2CD, EMI, Holland-Germany, 724349419025)/CD2`

### Magica (Deluxe Edition NEGO 17)  (2)
    - `Magica (Deluxe Edition NEGO 17) CD 1`
        - artist: Dio  ·  year: 2013
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/2000 Magica/2000 Magica [2013 Niji NEGO 17 Germany]/CD 1`
    - `Magica (Deluxe Edition NEGO 17) CD 2`
        - artist: Dio  ·  year: 2013
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/2000 Magica/2000 Magica [2013 Niji NEGO 17 Germany]/CD 2`

### Magical Mystery Tour (Mono Version)  (2)
    - `Magical Mystery Tour (Mono Version) (Disc 14)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/13-14 Magical Mystery Tour/14 Mono Version`
    - `Magical Mystery Tour (Stereo Version) (Disc 13)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/13-14 Magical Mystery Tour/13 Stereo Version`

### Mandatory Metallica (US Promo CD)  (2)
    - `Mandatory Metallica (US Promo CD, Disc 1)`
        - artist: Metallica  ·  year: 1997
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/1996 - Mandatory Metallica/1997 USA 2 CD Promo sampler, Elektra PRCD 9927-2/Disc 1`
    - `Mandatory Metallica (US Promo CD, Disc 2)`
        - artist: Metallica  ·  year: 1997
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/1996 - Mandatory Metallica/1997 USA 2 CD Promo sampler, Elektra PRCD 9927-2/Disc 2`

### Mastercutor Alive (GCR 20042L-2)  (2)
    - `Mastercutor Alive (GCR 20042L-2) CD 1`
        - artist: U.D.O.  ·  year: 2008
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2008 Mastercutor Alive/2008 Mastercutor Alive [2008 ZYX GCR 20042L-2 Germany]/CD 1`
    - `Mastercutor Alive (GCR 20042L-2) CD 2`
        - artist: U.D.O.  ·  year: 2008
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2008 Mastercutor Alive/2008 Mastercutor Alive [2008 ZYX GCR 20042L-2 Germany]/CD 2`

### MDNA (Deluxe Edition)  (4)
    - `MDNA (Deluxe Edition) Disc 1`
        - artist: Madonna  ·  year: 2012
        - folder: `D:/music/_cd_rip/Madonna - Discography/2012 - MDNA (Deluxe Edition) (2012 - Boy Toy, Inc. • Interscope Records • Live Nation Inc. - Germany - 0602527997360)/Disc One`
    - `MDNA (Deluxe Edition) Disc 2`
        - artist: Madonna  ·  year: 2012
        - folder: `D:/music/_cd_rip/Madonna - Discography/2012 - MDNA (Deluxe Edition) (2012 - Boy Toy, Inc. • Interscope Records • Live Nation Inc. - Germany - 0602527997360)/Disc Two`
    - `MDNA (Disc 1)`
        - artist: Madonna  ·  year: 2012
        - folder: `D:/music/Madonna 2012-MDNA [Deluxe Edition]`
    - `MDNA (Disc 2)`
        - artist: Madonna  ·  year: 2012
        - folder: `D:/music/Madonna 2012-MDNA [Deluxe Edition]`

### Melodic Still Rocks  (2)
    - `Melodic Still Rocks, Disc 1`
        - artist: Various  ·  year: 2006
        - folder: `D:/music/VA - Melodic Rock [FLAC]/2007 - Volume 4 - Melodic Still Rocks/CD1`
    - `Melodic Still Rocks, Disc 2`
        - artist: Various  ·  year: 2007
        - folder: `D:/music/VA - Melodic Rock [FLAC]/2007 - Volume 4 - Melodic Still Rocks/CD2`

### Metaforce  (2)
    - `Metaforce (CDSingle1)`
        - artist: Art Of Noise  ·  year: 1999
        - folder: `D:/music/Art of Noise - Metaforce (CDS1+CDS2) - 1999/Art of Noise - Metaforce (CDSingle1) - 1999`
    - `Metaforce (CDSingle2)`
        - artist: Art Of Noise  ·  year: 1999
        - folder: `D:/music/Art of Noise - Metaforce (CDS1+CDS2) - 1999/Art of Noise - Metaforce (CDSingle2) - 1999`

### Metal Works '73-'93 (1993, Columbia, 473050 2, Austria)  (4)
    - `Metal Works '73-'93 (1993, Columbia, 473050 2, Austria, CD 1)`
        - artist: Judas Priest  ·  year: 1993
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/1993 - Metal Works '73-'93 [Columbia, 473050 2, Austria]`
    - `Metal Works '73-'93 (1993, Columbia, 473050 2, Austria, CD 2)`
        - artist: Judas Priest  ·  year: 1993
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/1993 - Metal Works '73-'93 [Columbia, 473050 2, Austria]`
    - `Metal Works '73-'93 (1993, Epic / Sony, ESCA 5750, Japan, CD 1)`
        - artist: Judas Priest  ·  year: 1993
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/1993 - Metal Works '73-'93 [Epic-Sony, ESCA-5750, Japan]`
    - `Metal Works '73-'93 (1993, Epic / Sony, ESCA 5751, Japan, CD 2)`
        - artist: Judas Priest  ·  year: 1993
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/1993 - Metal Works '73-'93 [Epic-Sony, ESCA-5750, Japan]`

### Metalogy (2004, Sony, CK 87127, USA)  (8)
    - `Metalogy (2004, Sony, CK 87127, USA, CD 1)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, C4K 871 26, USA, 4 CD-Bonus DVD]`
    - `Metalogy (2004, Sony, CK 87128, USA, CD 2)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, C4K 871 26, USA, 4 CD-Bonus DVD]`
    - `Metalogy (2004, Sony, CK 87129, USA, CD 3)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, C4K 871 26, USA, 4 CD-Bonus DVD]`
    - `Metalogy (2004, Sony, CK 87130, USA, CD 4)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, C4K 871 26, USA, 4 CD-Bonus DVD]`
    - `Metalogy (2004, Sony, MHCP 312, Japan, CD 1)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, MHCP 312~16, Japan]/CD1`
    - `Metalogy (2004, Sony, MHCP 313, Japan, CD 2)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, MHCP 312~16, Japan]/CD2`
    - `Metalogy (2004, Sony, MHCP 314, Japan, CD 3)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, MHCP 312~16, Japan]/CD3`
    - `Metalogy (2004, Sony, MHCP 315, Japan, CD 4)`
        - artist: Judas Priest  ·  year: 2004
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2004 - Metalogy [Sony, MHCP 312~16, Japan]/CD4`

### Metropolis Part 1... Live (Summerfest Milwaukee June '93)  (2)
    - `Metropolis Part 1... Live (Summerfest Milwaukee June '93) CD 1`
        - artist: Dream Theater  ·  year: 2016
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2016-Metropolis Part 1... Live (Summerfest Milwaukee June '93)`
    - `Metropolis Part 1... Live (Summerfest Milwaukee June '93) CD 2`
        - artist: Dream Theater  ·  year: 2016
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/2016-Metropolis Part 1... Live (Summerfest Milwaukee June '93)`

### Milestones  (2)
    - `Milestones CD 1`
        - artist: Uriah Heep  ·  year: 1989
        - folder: `D:/music/_cd_rip/Uriah Heep - Discography/Compilations/1989. Milestones/1989. Milestones (Castle MSSCD 109 1-2, Sweden)/CD1`
    - `Milestones CD 2`
        - artist: Uriah Heep  ·  year: 1989
        - folder: `D:/music/_cd_rip/Uriah Heep - Discography/Compilations/1989. Milestones/1989. Milestones (Castle MSSCD 109 1-2, Sweden)/CD2`

### Millennium - 1960-1964  (2)
    - `Millennium - 1960-1964 (CD 1)`
        - artist: Wanda Jackson, Ricky Nelson, Del Shannon  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1960-1964 (2xCD)/Cd 1`
    - `Millennium - 1960-1964 (CD 2)`
        - artist: Manfred Mann, Herman's Hermits, Freddie & The Dreamers  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1960-1964 (2xCD)/Cd 2`

### Millennium - 1970-1974 (2CDs)  (2)
    - `Millennium - 1970-1974 (2CDs) (CD1)`
        - artist: Mungo Jerry, Hot Butter, Mud  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1970-1974 (2xCD)/Cd 1`
    - `Millennium - 1970-1974 (2CDs) (CD2)`
        - artist: The Guess Who, 10cc, Ike & Tina Turner  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1970-1974 (2xCD)/Cd 2`

### Millennium - 1995-1998  (2)
    - `Millennium - 1995-1998 (CD 1)`
        - artist: Los del Rio, Rednex, The Outhere Brothers  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1995-1998 (2xCD)/Cd 1`
    - `Millennium - 1995-1998 (CD2)`
        - artist: Vengaboys, Sash!, Nakatomi  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1995-1998 (2xCD)/Cd 2`

### Millennium 1950 - 1954 (2 CD's) (CD 1/2)  (2)
    - `Millennium  1950 - 1954   (2 CD's) (CD 1/2)`
        - artist: Kay Starr, Nat King Cole, Dean Martin  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1950-1954 (2xCD)/Cd 1`
    - `Millennium  1950 - 1954   (2 CD's) (CD 2/2)`
        - artist: Les Baxter, Dean Martin, The Four Freshmen  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1950-1954 (2xCD)/Cd 2`

### Millennium 1965 - 1969 (2 CD's) (CD 1/2)  (2)
    - `Millennium  1965 - 1969   (2 CD's) (CD 1/2)`
        - artist: The Beach Boys, The Mamas & The Papas, The Flowerpot Men  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1965-1969 (2xCD)/Cd 1`
    - `Millennium  1965 - 1969   (2 CD's) (CD 2/2)`
        - artist: Shocking Blue, Zager & Evans, John Fred & The Playboys  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1965-1969 (2xCD)/Cd 2`

### Millennium 1980 - 1984 (2 CD's) (CD 1/2)  (2)
    - `Millennium  1980 - 1984   (2 CD's) (CD 1/2)`
        - artist: Kids In America, Feels Like I'm In Love, Stars On 45 (Medley)  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1980-1984 (2xCD)/Cd 1`
    - `Millennium  1980 - 1984   (2 CD's) (CD 2/2)`
        - artist: Culture Club, Kajagoogoo, Pat Benatar  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1980-1984 (2xCD)/Cd 2`

### Millennium 1985 - 1989 (2 CD's) (CD 1/2)  (2)
    - `Millennium  1985 - 1989   (2 CD's) (CD 1/2)`
        - artist: Katrina & The Waves, Belinda Carlisle, Starship  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1985-1989 (2xCD)/Cd 1`
    - `Millennium  1985 - 1989   (2 CD's) (CD 2/2)`
        - artist: Tell It To My Heart, Here I Go Again, I Think We're Alone Now  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1985-1989 (2xCD)/Cd 2`

### Millennium 1990 - 1994  (2)
    - `Millennium 1990 - 1994  CD1`
        - artist: Snap, Mc Hammer, Vanilla Ice  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1990-1994 (2xCD)/Cd 1`
    - `Millennium 1990 - 1994  CD2`
        - artist: Tag Team, 2 Unlimited, Jazzy Jeff & The Fresh Prince  ·  year: 1998
        - folder: `D:/music/_cd_rip/The Millenium Collection - The Best Pop Music Of The 20th Century(18 CD) {1998}/Millennium - 1990-1994 (2xCD)/Cd 2`

### Mit 80 Jahren um die Welt  (2)
    - `Mit 80 Jahren um die Welt - CD 1`
        - artist: James Last  ·  year: 2008
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2008 - Mit 80 Jahren um die Welt [Universal Music 06007 5312995 (1)] 4CD/CD1`
    - `Mit 80 Jahren um die Welt - CD 3`
        - artist: James Last  ·  year: 2008
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2008 - Mit 80 Jahren um die Welt [Universal Music 06007 5312995 (1)] 4CD/CD3`

### Mk III : The Final Concerts (1997, 2CD, ACH 80003)  (2)
    - `Mk III : The Final Concerts CD1 (1997, 2CD, ACH 80003)`
        - artist: Deep Purple  ·  year: 1975
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1996. Mk III The Final Concerts (1997, 2CD, Archive Records, USA, ACH 80003)/CD1`
    - `Mk III : The Final Concerts CD2 (1997, 2CD, ACH 80003)`
        - artist: Deep Purple  ·  year: 1975
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1996. Mk III The Final Concerts (1997, 2CD, Archive Records, USA, ACH 80003)/CD2`

### Mk III The Final Concerts (2001, 2CD, ER202322)  (2)
    - `Mk III The Final Concerts CD1 (2001, 2CD, ER202322)`
        - artist: Deep Purple  ·  year: 1996
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1996. Mk III The Final Concerts (2001, 2CD, Eagle Records, USA, ER202322)/CD1`
    - `Mk III The Final Concerts CD2 (2001, 2CD, ER202322)`
        - artist: Deep Purple  ·  year: 1996
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1996. Mk III The Final Concerts (2001, 2CD, Eagle Records, USA, ER202322)/CD2`

### Mob Rules (Deluxe Expanded Edition)  (2)
    - `Mob Rules (Deluxe Expanded Edition, CD1)`
        - artist: Black Sabbath  ·  year: 1981
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1981 Mob Rules/1981 Mob Rules [2010 Germany 2735070 Sanctuary]/CD1`
    - `Mob Rules (Deluxe Expanded Edition, CD2)`
        - artist: Black Sabbath  ·  year: 1981
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1981 Mob Rules/1981 Mob Rules [2010 Germany 2735070 Sanctuary]/CD2`

### More Gold  (2)
    - `More Gold (disc 1)`
        - artist: Romantic Collection  ·  year: 2005
        - folder: `D:/music/_cd_rip/Romantic Collection/03. More Gold/More_Gold_CD-1`
    - `More Gold (disc 2)`
        - artist: Romantic Collection  ·  year: 2005
        - folder: `D:/music/_cd_rip/Romantic Collection/03. More Gold/More_Gold_CD-2`

### More Hot Rocks (Big Hits & Fazed Cookies) (96262)  (6)
    - `More Hot Rocks (Big Hits & Fazed Cookies)  (CD 1) (96262)`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1972 More Hot Rocks (Big Hits & Fazed Cookies)/More Hot Rocks [ABKCO 96262]/Disc 1`
    - `More Hot Rocks (Big Hits & Fazed Cookies) (CD 2)  (96262)`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1972 More Hot Rocks (Big Hits & Fazed Cookies)/More Hot Rocks [ABKCO 96262]/Disc 2`
    - `More Hot Rocks (Big Hits & Fazed Cookies) Disc 1 (UICY-93033)`
        - artist: The Rolling Stones  ·  year: 2006
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1972 More Hot Rocks (Big Hits & Fazed Cookies)/More Hot Rocks [UICY-93033-4]/Disc 1`
    - `More Hot Rocks (Big Hits & Fazed Cookies) Disc 2 (UICY-93034)`
        - artist: The Rolling Stones  ·  year: 2006
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1972 More Hot Rocks (Big Hits & Fazed Cookies)/More Hot Rocks [UICY-93033-4]/Disc 2`
    - `More Hot Rocks (Big Hits and Fazed Cookies) [CD 1]`
        - artist: The Rolling Stones  ·  year: 1972
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1972 More Hot Rocks (Big Hits & Fazed Cookies)/More Hot Rocks [ABKCO 62672]/Disc 1`
    - `More Hot Rocks (Big Hits and Fazed Cookies) [CD 2]`
        - artist: The Rolling Stones  ·  year: 1972
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1972 More Hot Rocks (Big Hits & Fazed Cookies)/More Hot Rocks [ABKCO 62672]/Disc 2`

### Mothership  (4)
    - `Mothership  disc 1`
        - artist: Led Zeppelin  ·  year: 2007
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/2007. Mothership/2007. Led Zeppelin - Mothership (Atlantic-Swan Song WPCR-14841-2, Japan)/disc1`
    - `Mothership CD1`
        - artist: Led Zeppelin  ·  year: 2007
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/2007. Mothership/2007. Led Zeppelin - Mothership (Atlantic-Swan Song 081227950934, EU)/CD1`
    - `Mothership CD2`
        - artist: Led Zeppelin  ·  year: 2015
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/2007. Mothership/2007. Led Zeppelin - Mothership (Atlantic-Swan Song 081227950934, EU)/CD2`
    - `Mothership disc 2`
        - artist: Led Zeppelin  ·  year: 2007
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/2007. Mothership/2007. Led Zeppelin - Mothership (Atlantic-Swan Song WPCR-14841-2, Japan)/disc2`

### MRCD Volume 7 (Forces Of Dark & Light)  (2)
    - `MRCD Volume 7 (Forces Of Dark & Light, Disc 1)`
        - artist: Jim Peterik, Hodson, Degreed  ·  year: 2010
        - folder: `D:/music/VA - Melodic Rock [FLAC]/2010 - Volume 7 - Forces Of Dark & Light/CD1-My War`
    - `MRCD Volume 7 (Forces Of Dark & Light, Disc 2)`
        - artist: W.E.T., Vega, First Signal  ·  year: 2010
        - folder: `D:/music/VA - Melodic Rock [FLAC]/2010 - Volume 7 - Forces Of Dark & Light/CD2-When You Believe`

### MTV Unplugged In Athens  (5)
    - `MTV Unplugged In Athens CD 1`
        - artist: Scorpions  ·  year: 2013
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2013 EU 88843 01432 2 RCA]/CD 1`
    - `MTV Unplugged In Athens CD 2`
        - artist: Scorpions  ·  year: 2013
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2013 EU 88843 01432 2 RCA]/CD 2`
    - `MTV Unplugged In Athens CD1`
        - artist: Scorpions  ·  year: 2013
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2013 EU 88843010322 Sony]/CD 1`
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2013 Japan SICP 3927~8 Sony]/CD 1`
    - `MTV Unplugged In Athens CD2`
        - artist: Scorpions  ·  year: 2013
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2013 EU 88843010322 Sony]/CD 2`
    - `MTV Unplugged in Athens CD2`
        - artist: Scorpions  ·  year: 2013
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2013 Japan SICP 3927~8 Sony]/CD 2`

### MTV Unplugged: Live In Athens  (3)
    - `MTV Unplugged: Live In Athens CD 1`
        - artist: Scorpions  ·  year: 2014
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2014 EU 888430 05427 2 Sony]/CD 1`
    - `MTV Unplugged: Live In Athens CD 2`
        - artist: Scorpions  ·  year: 2014
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2014 EU 888430 05427 2 Sony]/CD 2`
    - `MTV Unplugged: Live In Athens CD 3`
        - artist: Scorpions  ·  year: 2014
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/2013 MTV Unplugged In Athens/2013 MTV Unplugged In Athens [2014 EU 888430 05427 2 Sony]/CD 3`

### Music To Crash Your Car To - Vol. 1  (2)
    - `Music To Crash Your Car To - Vol. 1 - Disc 1`
        - artist: Motley Crue  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2003 - Music To Crash Your Car To - Vol. 1 [Hip-O Rec., B0001460-02, USA]/Motley Crue - Music To Crash Your Car To - Vol. 1 - Disc 1`
    - `Music To Crash Your Car To - Vol. 1 - Disc 4`
        - artist: Motley Crue  ·  year: 2003
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2003 - Music To Crash Your Car To - Vol. 1 [Hip-O Rec., B0001460-02, USA]/Motley Crue - Music To Crash Your Car To - Vol. 1 - Disc 4`

### Music To Crash Your Car To - Vol. 2  (4)
    - `Music To Crash Your Car To - Vol. 2 - Disc 1`
        - artist: Motley Crue  ·  year: 2004
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2004 - Music To Crash Your Car To - Vol. 2 [Hip-O Rec., B0002839-02, USA]/Motley Crue - Music To Crash Your Car To - Vol. 2 - Disc 1`
    - `Music To Crash Your Car To - Vol. 2 - Disc 2`
        - artist: Motley Crue  ·  year: 2004
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2004 - Music To Crash Your Car To - Vol. 2 [Hip-O Rec., B0002839-02, USA]/Motley Crue - Music To Crash Your Car To - Vol. 2 - Disc 2`
    - `Music To Crash Your Car To - Vol. 2 - Disc 3`
        - artist: Motley Crue  ·  year: 2004
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2004 - Music To Crash Your Car To - Vol. 2 [Hip-O Rec., B0002839-02, USA]/Motley Crue - Music To Crash Your Car To - Vol. 2 - Disc 3`
    - `Music To Crash Your Car To - Vol. 2 - Disc 4`
        - artist: Motley Crue  ·  year: 2004
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2004 - Music To Crash Your Car To - Vol. 2 [Hip-O Rec., B0002839-02, USA]/Motley Crue - Music To Crash Your Car To - Vol. 2 - Disc 4`

### Mylenium Tour  (2)
    - `Mylenium Tour CD1`
        - artist: Mylene Farmer  ·  year: 2000
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2000 - Mylenium Tour (2CD)/Mylene Farmer - Mylenium Tour CD1`
    - `Mylenium Tour CD2`
        - artist: Mylene Farmer  ·  year: 2000
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2000 - Mylenium Tour (2CD)/Mylene Farmer - Mylenium Tour CD2`

### Navy Metal Night  (2)
    - `Navy Metal Night (CD1)`
        - artist: U.D.O.  ·  year: 2015
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2015 Navy Metal Night/2015 Navy Metal Night [2015 AFM 525-7 Germany]/CD1`
    - `Navy Metal Night (CD2)`
        - artist: U.D.O.  ·  year: 2015
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2015 Navy Metal Night/2015 Navy Metal Night [2015 AFM 525-7 Germany]/CD2`

### No Prayer For The Dying (7243 8 35876 2 9)  (4)
    - `No Prayer For The Dying (7243 8 35876 2 9, CD1)`
        - artist: Iron Maiden  ·  year: 1990
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1990 No Prayer For The Dying/1990 No Prayer For The Dying [1995 Holland 7243 8 35876 2 9 EMI]/CD 1`
    - `No Prayer For The Dying (7243 8 35876 2 9, CD2)`
        - artist: Iron Maiden  ·  year: 1990
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1990 No Prayer For The Dying/1990 No Prayer For The Dying [1995 Holland 7243 8 35876 2 9 EMI]/CD 2`
    - `No Prayer For The Dying [Castle 110-2] CD 1`
        - artist: Iron Maiden  ·  year: 1990
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1990 No Prayer For The Dying/1990 No Prayer For The Dying [1995 USA CASTLE 110-2. Castle Records]/CD 1`
    - `No Prayer For The Dying [Castle 110-2] CD 2`
        - artist: Iron Maiden  ·  year: 1990
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1990 No Prayer For The Dying/1990 No Prayer For The Dying [1995 USA CASTLE 110-2. Castle Records]/CD 2`

### No Remorse (1996, UK, Castle, ESD CD 371)  (4)
    - `No Remorse (1996, UK, Castle, ESD CD 371) (CD1)`
        - artist: Motorhead  ·  year: 1984
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/1984-No Remorse/1984-No Remorse (1996, UK, Castle, ESD CD 371, 2CD)/CD1`
    - `No Remorse (1996, UK, Castle, ESD CD 371) (CD2)`
        - artist: Motorhead  ·  year: 1984
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/1984-No Remorse/1984-No Remorse (1996, UK, Castle, ESD CD 371, 2CD)/CD2`
    - `No Remorse (2010, EU, Sanctuary, 2748857) (CD1)`
        - artist: Motorhead  ·  year: 1984
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/1984-No Remorse/1984-No Remorse (2010, EU, Sanctuary, 2748857, 2CD)/CD1`
    - `No Remorse (2010, EU, Sanctuary, 2748857) (CD2)`
        - artist: Motorhead  ·  year: 1984
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/1984-No Remorse/1984-No Remorse (2010, EU, Sanctuary, 2748857, 2CD)/CD2`

### No Security San Jose 99  (2)
    - `No Security San Jose 99 (CD1)`
        - artist: The Rolling Stones  ·  year: 2018
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2018 No Security. San Jose 99 [EAGDV096]/CD1`
    - `No Security San Jose 99 (CD2)`
        - artist: The Rolling Stones  ·  year: 2018
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2018 No Security. San Jose 99 [EAGDV096]/CD2`

### No Sleep 'til Hammersmith (2008, 0602517855755)  (2)
    - `No Sleep 'til Hammersmith (2008, 0602517855755, CD1)`
        - artist: Motorhead  ·  year: 1981
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/1981-No Sleep 'til Hammersmith/1981-No Sleep 'til Hammersmith (2008, EU, Sanctuary, 0602517855755, 2CD)/CD1`
    - `No Sleep 'til Hammersmith (2008, 0602517855755, CD2)`
        - artist: Motorhead  ·  year: 1981
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/1981-No Sleep 'til Hammersmith/1981-No Sleep 'til Hammersmith (2008, EU, Sanctuary, 0602517855755, 2CD)/CD2`

### Nobody's Perfect (2009, 2CD, UICY 75502-3)  (2)
    - `Nobody's Perfect CD1 (2009, 2CD, UICY 75502-3)`
        - artist: Deep Purple  ·  year: 1988
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1988. Nobody's Perfect (2009, 2CD, Universal, Japan, UICY 75502-3)/CD1`
    - `Nobody's Perfect CD2 (2009, 2CD, UICY 75502-3)`
        - artist: Deep Purple  ·  year: 1988
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1988. Nobody's Perfect (2009, 2CD, Universal, Japan, UICY 75502-3)/CD2`

### Nostradamus (2008, Sony / BMG, 88697315512-S2, Limited Edition Book, Germany)  (4)
    - `Nostradamus (2008, Sony / BMG, 88697315512-S2, Limited Edition Book, Germany, CD 1)`
        - artist: Judas Priest  ·  year: 2008
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/2008 - Nostradamus/2008 - Nostradamus [Sony, 88697315512-S2, Germany]`
    - `Nostradamus (2008, Sony / BMG, 88697315512-S2, Limited Edition Book, Germany, CD 2)`
        - artist: Judas Priest  ·  year: 2008
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/2008 - Nostradamus/2008 - Nostradamus [Sony, 88697315512-S2, Germany]`
    - `Nostradamus (2012, Sony / Epic, 88697967872-JK16, USA, CD 1)`
        - artist: Judas Priest  ·  year: 2008
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2012 - The Complete Albums Collection [19 CD Boxset, Sony-Legacy, 88697967872, USA]/2008 - Nostradamus [Epic, 88697967872-J16]/CD1`
    - `Nostradamus (2012, Sony / Epic, 88697967872-JK16, USA, CD 2)`
        - artist: Judas Priest  ·  year: 2008
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2012 - The Complete Albums Collection [19 CD Boxset, Sony-Legacy, 88697967872, USA]/2008 - Nostradamus [Epic, 88697967872-J16]/CD2`

### November Rain at the Tokyo Dome  (2)
    - `November Rain at the Tokyo Dome - Disk 1`
        - artist: Guns N' Roses  ·  year: 1992
        - folder: `D:/music/Guns N' Roses - 1992 - November Rain at the Tokyo Dome (22.02.1992)/CD1`
    - `November Rain at the Tokyo Dome - Disk 2`
        - artist: Guns N' Roses  ·  year: 1992
        - folder: `D:/music/Guns N' Roses - 1992 - November Rain at the Tokyo Dome (22.02.1992)/CD2`

### Now That's What I Call Music! 1980  (2)
    - `Now That's What I Call Music! 1980 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1980/CD 1`
    - `Now That's What I Call Music! 1980 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1980/CD 2`

### Now That's What I Call Music! 1981  (2)
    - `Now That's What I Call Music! 1981 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1981/CD 1`
    - `Now That's What I Call Music! 1981 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1981/CD 2`

### Now That's What I Call Music! 1982  (2)
    - `Now That's What I Call Music! 1982 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1982/CD 1`
    - `Now That's What I Call Music! 1982 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1982/CD 2`

### Now That's What I Call Music! 1983  (2)
    - `Now That's What I Call Music! 1983 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1983/CD 1`
    - `Now That's What I Call Music! 1983 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1983/CD 2`

### Now That's What I Call Music! 1986  (2)
    - `Now That's What I Call Music! 1986 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1986/CD 1`
    - `Now That's What I Call Music! 1986 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1986/CD 2`

### Now That's What I Call Music! 1987  (2)
    - `Now That's What I Call Music! 1987 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1987/CD 1`
    - `Now That's What I Call Music! 1987 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1987/CD 2`

### Now That's What I Call Music! 1988  (2)
    - `Now That's What I Call Music! 1988 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1988/CD 1`
    - `Now That's What I Call Music! 1988 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1988/CD 2`

### Now That's What I Call Music! 1989  (2)
    - `Now That's What I Call Music! 1989 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1989/CD 1`
    - `Now That's What I Call Music! 1989 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1989/CD 2`

### Now That's What I Call Music! 1990  (2)
    - `Now That's What I Call Music! 1990 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1990/CD 1`
    - `Now That's What I Call Music! 1990 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1990/CD 2`

### Now That's What I Call Music! 1991  (2)
    - `Now That's What I Call Music! 1991 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1991/CD 1`
    - `Now That's What I Call Music! 1991 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1991/CD 2`

### Now That's What I Call Music! 1992  (2)
    - `Now That's What I Call Music! 1992 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1992/CD 1`
    - `Now That's What I Call Music! 1992 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1992/CD 2`

### Now That's What I Call Music! 1993  (2)
    - `Now That's What I Call Music! 1993 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1993/CD 1`
    - `Now That's What I Call Music! 1993 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1993/CD 2`

### Now That's What I Call Music! 1994  (2)
    - `Now That's What I Call Music! 1994 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1994/CD 1`
    - `Now That's What I Call Music! 1994 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1994/CD 2`

### Now That's What I Call Music! 1995  (2)
    - `Now That's What I Call Music! 1995 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1995/CD 1`
    - `Now That's What I Call Music! 1995 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1995/CD 2`

### Now That's What I Call Music! 1996  (2)
    - `Now That's What I Call Music! 1996 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1996/CD 1`
    - `Now That's What I Call Music! 1996 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1996/CD 2`

### Now That's What I Call Music! 1997  (2)
    - `Now That's What I Call Music! 1997 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1997/CD 1`
    - `Now That's What I Call Music! 1997 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1997/CD 2`

### Now That's What I Call Music! 1998  (2)
    - `Now That's What I Call Music! 1998 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1998/CD 1`
    - `Now That's What I Call Music! 1998 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1998/CD 2`

### Now That's What I Call Music! 1999  (2)
    - `Now That's What I Call Music! 1999 (CD1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1999/CD 1`
    - `Now That's What I Call Music! 1999 (CD2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - Now That's What I Call Music! - The Millennium Series/1999/CD 2`

### O, Yeah! Ultimate Aerosmith Hits  (3)
    - `O, Yeah! Ultimate Aerosmith Hits (CD1)`
        - artist: Aerosmith  ·  year: 2002
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Compilations/2002 - O, Yeah! Ultimate Aerosmith Hits/2002 - O, Yeah! Ultimate Aerosmith Hits (5084672000) Australia/CD 1`
    - `O, Yeah! Ultimate Aerosmith Hits (CD1) [SICP 170-1]`
        - artist: Aerosmith  ·  year: 2002
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Compilations/2002 - O, Yeah! Ultimate Aerosmith Hits/2002 - O, Yeah! Ultimate Aerosmith Hits (SICP 170-1) Japan/CD 1`
    - `O, Yeah! Ultimate Aerosmith Hits (CD2) [SICP 170-1]`
        - artist: Aerosmith  ·  year: 2002
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Compilations/2002 - O, Yeah! Ultimate Aerosmith Hits/2002 - O, Yeah! Ultimate Aerosmith Hits (SICP 170-1) Japan/CD 2`

### Octane Twisted  (2)
    - `Octane Twisted CD1`
        - artist: Porcupine Tree  ·  year: 2012
        - folder: `D:/music/Porcupine Tree Main discography/Live Albums/2012 - Octane Twisted 2CD (Kscope KSCOPE218 Germany)/CD1`
    - `Octane Twisted CD2`
        - artist: Porcupine Tree  ·  year: 2012
        - folder: `D:/music/Porcupine Tree Main discography/Live Albums/2012 - Octane Twisted 2CD (Kscope KSCOPE218 Germany)/CD2`

### Official Bootleg: Falling Into Infinity Demos 1996-1997  (2)
    - `Official Bootleg: Falling Into Infinity Demos 1996-1997 CD 1`
        - artist: Dream Theater  ·  year: 2007
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2007-Falling Into Infinity Demos 1996-1997`
    - `Official Bootleg: Falling Into Infinity Demos 1996-1997 CD 2`
        - artist: Dream Theater  ·  year: 2007
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2007-Falling Into Infinity Demos 1996-1997`

### Official Bootleg: House Of Blues Los Angeles, California 5/18/98  (2)
    - `Official Bootleg: House Of Blues Los Angeles, California 5/18/98 CD 1`
        - artist: Dream Theater  ·  year: 2003
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2003-Los Angeles, California 5-18-98`
    - `Official Bootleg: House Of Blues Los Angeles, California 5/18/98 CD 2`
        - artist: Dream Theater  ·  year: 2003
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2003-Los Angeles, California 5-18-98`

### Official Bootleg: Live At Birch Hill Night Club In Old Bridge, NJ 12/14/96  (2)
    - `Official Bootleg: Live At Birch Hill Night Club In Old Bridge, NJ 12/14/96 CD 1`
        - artist: Dream Theater  ·  year: 2006
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2006-Old Bridge, New Jersey 12-14-96`
    - `Official Bootleg: Live At Birch Hill Night Club In Old Bridge, NJ 12/14/96 CD 2`
        - artist: Dream Theater  ·  year: 2006
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2006-Old Bridge, New Jersey 12-14-96`

### Official Bootleg: Live At NHK Hall In Tokyo, Japan - 10/28/95  (2)
    - `Official Bootleg: Live At NHK Hall In Tokyo, Japan - 10/28/95 CD 1`
        - artist: Dream Theater  ·  year: 2004
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2004-Tokyo, Japan 10-28-95`
    - `Official Bootleg: Live At NHK Hall In Tokyo, Japan - 10/28/95 CD 2`
        - artist: Dream Theater  ·  year: 2004
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2004-Tokyo, Japan 10-28-95`

### Official Bootleg: When Dream And Day Unite Demos  (2)
    - `Official Bootleg: When Dream And Day Unite Demos CD 1`
        - artist: Dream Theater  ·  year: 2004
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2004-When Dream And Day Unite Demos 1987-1989`
    - `Official Bootleg: When Dream And Day Unite Demos CD 2`
        - artist: Dream Theater  ·  year: 2004
        - folder: `D:/music/_cd_rip/Dream Theater/Official Bootlegs/2004-When Dream And Day Unite Demos 1987-1989`

### On Air - Live At The BBC Volume 2  (2)
    - `On Air - Live At The BBC Volume 2 (Disc 1)`
        - artist: The Beatles  ·  year: 2013
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/2013 - The Beatles On Air - Live At The BBC Vol. 2/Disc 1`
    - `On Air - Live At The BBC Volume 2 (Disc 2)`
        - artist: The Beatles  ·  year: 2013
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/2 Live/2013 - The Beatles On Air - Live At The BBC Vol. 2/Disc 2`

### Once In A LIVEtime  (2)
    - `Once In A LIVEtime, Disc 2`
        - artist: Dream Theater  ·  year: 1998
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/1998-Once In A Livetime`
    - `Once in a LIVEtime, Disc 1`
        - artist: Dream Theater  ·  year: 1998
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/1998-Once In A Livetime`

### Once Upon A Time In South America (2015, 4CD, ROC-CD-3301)  (4)
    - `Once Upon A Time In South America CD1 (2015, 4CD, ROC-CD-3301)`
        - artist: Emerson, Lake & Palmer  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2015. Emerson, Lake & Palmer - Once Upon A Time In South America (2015, 4CD, Leadclass, USA, ROC-CD-3301)/CD1`
    - `Once Upon A Time In South America CD2 (2015, 4CD, ROC-CD-3301)`
        - artist: Emerson, Lake & Palmer  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2015. Emerson, Lake & Palmer - Once Upon A Time In South America (2015, 4CD, Leadclass, USA, ROC-CD-3301)/CD2`
    - `Once Upon A Time In South America CD3 (2015, 4CD, ROC-CD-3301)`
        - artist: Emerson, Lake & Palmer  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2015. Emerson, Lake & Palmer - Once Upon A Time In South America (2015, 4CD, Leadclass, USA, ROC-CD-3301)/CD3`
    - `Once Upon A Time In South America CD4 (2015, 4CD, ROC-CD-3301)`
        - artist: Emerson, Lake & Palmer  ·  year: 2015
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2015. Emerson, Lake & Palmer - Once Upon A Time In South America (2015, 4CD, Leadclass, USA, ROC-CD-3301)/CD4`

### One Nite Alone... Live!  (2)
    - `One Nite Alone... Live! CD1`
        - artist: Prince & The NPG  ·  year: 2002
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/2002 - One Nite Alone... Live! (Prince & The NPG)/CD1`
    - `One Nite Alone... Live! CD2`
        - artist: Prince & The New Power Generation  ·  year: 2002
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/2002 - One Nite Alone... Live! (Prince & The NPG)/CD2`

### One Shot '80 - Volume 10  (2)
    - `One Shot '80 - Volume 10 (Disc 1)`
        - artist: Various Artists  ·  year: 2000
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 10 (2000) [FLAC]/Disc 1of2`
    - `One Shot '80 - Volume 10 (Disc 2)`
        - artist: Various Artists  ·  year: 2000
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 10 (2000) [FLAC]/Disc 2of2`

### One Shot '80 - Volume 15  (2)
    - `One Shot '80 - Volume 15 (Disc 1)`
        - artist: Various Artists  ·  year: 2003
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 15 (2003) [FLAC]/Disc 1of2`
    - `One Shot '80 - Volume 15 (Disc 2)`
        - artist: Various Artists  ·  year: 2003
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 15 (2003) [FLAC]/Disc 2of2`

### One Shot '80 - Volume 20  (2)
    - `One Shot '80 - Volume 20 (Disc 1)`
        - artist: Various Artists  ·  year: 2009
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 20 (2009) [FLAC]/Disc 1of2`
    - `One Shot '80 - Volume 20 (Disc 2)`
        - artist: Various Artists  ·  year: 2009
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 20 (2009) [FLAC]/Disc 2of2`

### One Shot '80 - Volume 5  (2)
    - `One Shot '80 - Volume 5 (Disc 1)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 05 (1999) [FLAC]/Disc 1of2`
    - `One Shot '80 - Volume 5 (Disc 2)`
        - artist: Various Artists  ·  year: 1999
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Volume 05 (1999) [FLAC]/Disc 2of2`

### One Shot Black '80  (2)
    - `One Shot Black  '80 (CD 1)`
        - artist: Various  ·  year: 2004
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Black '80 (2004) [FLAC]/Various - One Shot Black '80 (CD 1)`
    - `One Shot Black '80 (CD 2)`
        - artist: Various  ·  year: 2004
        - folder: `D:/music/_cd_rip/VA - One Shot '80/Various Artists - One Shot '80 - Black '80 (2004) [FLAC]/Various - One Shot Black '80 (CD 2)`

### Orgasmatron (2006, USA, Sanctuary, 06076-86418-2)  (2)
    - `Orgasmatron (2006, USA, Sanctuary, 06076-86418-2, CD1)`
        - artist: Motorhead  ·  year: 1986
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1986-Orgasmatron/1986-Orgasmatron (2006, USA, Sanctuary, 06076-86418-2, 2CD)/CD1`
    - `Orgasmatron (2006, USA, Sanctuary, 06076-86418-2, CD2)`
        - artist: Motorhead  ·  year: 1986
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1986-Orgasmatron/1986-Orgasmatron (2006, USA, Sanctuary, 06076-86418-2, 2CD)/CD2`

### Orgullo, Pasion, Y Gloria  (2)
    - `Orgullo, Pasion, Y Gloria (CD 1)`
        - artist: Metallica  ·  year: 2009
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/2009 - Orgullo, Pasion, Y Gloria (2 CD)/Disc 1`
    - `Orgullo, Pasion, Y Gloria (CD 2)`
        - artist: Metallica  ·  year: 2009
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/2009 - Orgullo, Pasion, Y Gloria (2 CD)/Disc 2`

### Overkill (2005, USA, 06076-86407-2)  (2)
    - `Overkill (2005, USA, 06076-86407-2, CD1)`
        - artist: Motorhead  ·  year: 1979
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1979a-Overkill/1979-Overkill (2005, USA, Sanctuary, 06076-86407-2, 2CD)/CD1`
    - `Overkill (2005, USA, 06076-86407-2, CD2)`
        - artist: Motorhead  ·  year: 1979
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1979a-Overkill/1979-Overkill (2005, USA, Sanctuary, 06076-86407-2, 2CD)/CD2`

### Pain Of Mind (Japan)  (2)
    - `Pain Of Mind (Japan) CD 2`
        - artist: Neurosis  ·  year: 2000
        - folder: `D:/music/_cd_rip/Neurosis/1987-Pain Of Mind/2000-Pain Of Mind [Howling Bull Entertainment, Japan, HWCY-1038~1039]`
    - `Pain Of Mind (Reissue) CD 2`
        - artist: Neurosis  ·  year: 2000
        - folder: `D:/music/_cd_rip/Neurosis/1987-Pain Of Mind/2000-Pain Of Mind [Neurot Recordings, US, NR001]`

### Paranoid (Deluxe Expanded Edition)  (2)
    - `Paranoid (Deluxe Expanded Edition) CD1`
        - artist: Black Sabbath  ·  year: 1970
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1970 Paranoid/1970 Paranoid [2009 Germany 1782444 Sanctuary]/CD1`
    - `Paranoid (Deluxe Expanded Edition) CD2`
        - artist: Black Sabbath  ·  year: 1970
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1970 Paranoid/1970 Paranoid [2009 Germany 1782444 Sanctuary]/CD2`

### Paris 1975 (2012, 2CD, 0208332ERE)  (2)
    - `Paris 1975 CD1 (2012, 2CD, 0208332ERE)`
        - artist: Deep Purple  ·  year: 2012
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2003. Paris 1975 (2012, 2CD, earMUSIC, Germany, 0208332ERE)/CD1`
    - `Paris 1975 CD2 (2012, 2CD, 0208332ERE)`
        - artist: Deep Purple  ·  year: 2001
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2003. Paris 1975 (2012, 2CD, earMUSIC, Germany, 0208332ERE)/CD2`

### Past Lives  (2)
    - `Past Lives (CD1)`
        - artist: Black Sabbath  ·  year: 2002
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/2002 Past Lives/2002 Past Lives [2002 USA 06076-84561-2 Divine]/CD1`
    - `Past Lives (CD2)`
        - artist: Black Sabbath  ·  year: 2002
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/2002 Past Lives/2002 Past Lives [2002 USA 06076-84561-2 Divine]/CD2`

### Past Lives - Deluxe Edition  (2)
    - `Past Lives - Deluxe Edition CD1`
        - artist: Black Sabbath  ·  year: 2010
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/2002 Past Lives/2002 Past Lives [2010 Germany 2749907 Sanctuary]/CD1`
    - `Past Lives - Deluxe Edition CD2`
        - artist: Black Sabbath  ·  year: 2010
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/2002 Past Lives/2002 Past Lives [2010 Germany 2749907 Sanctuary]/CD2`

### Past Masters  (2)
    - `Past Masters (Disc 1)`
        - artist: The Beatles  ·  year: 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2009 - The Beatles Stereo Box Set/14 Past Masters/Disc 1`
    - `Past Masters (Disc 2)`
        - artist: The Beatles  ·  year: 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2009 - The Beatles Stereo Box Set/14 Past Masters/Disc 2`

### Past Times With Good Company  (2)
    - `Past Times With Good Company CD1`
        - artist: Blackmore's Night  ·  year: 2002
        - folder: `D:/music/_cd_rip/Blackmore's Night - Japanese Lossless Discography/Blackmore's Night - 2002 Past Times With Good Company/CD1`
    - `Past Times With Good Company CD2`
        - artist: Blackmore's Night  ·  year: 2002
        - folder: `D:/music/_cd_rip/Blackmore's Night - Japanese Lossless Discography/Blackmore's Night - 2002 Past Times With Good Company/CD2`

### Perfect Strangers Live (2013, 2CD+DVD, EAGDV026)  (2)
    - `Perfect Strangers Live CD1 (2013, 2CD+DVD, EAGDV026)`
        - artist: Deep Purple  ·  year: 1984
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2013. Perfect Strangers Live (2013, 2CD+DVD, Eagle Vision, Germany, EAGDV026)/CD1`
    - `Perfect Strangers Live CD2 (2013, 2CD+DVD, EAGDV026)`
        - artist: Deep Purple  ·  year: 1984
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2013. Perfect Strangers Live (2013, 2CD+DVD, Eagle Vision, Germany, EAGDV026)/CD2`

### Physical Graffiti  (13)
    - `Physical Graffiti (CD2)`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song SS 200-2, USA, JVC)/disc2`
    - `Physical Graffiti (Disc 1)`
        - artist: Led Zeppelin  ·  year: 1975, 2001
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1993. The Complete Studio Recording (Atlantic 7 82526-2, USA)/1975. Physical Graffiti/Disc 1`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song 289 400, Germany)/disc1`
    - `Physical Graffiti (Disc One)`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song SS 200-2, USA, WEA)/disc1`
    - `Physical Graffiti (Disc Two)`
        - artist: LED ZEPPELIN  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/1993. The Complete Studio Recording (Atlantic 7 82526-2, USA)/1975. Physical Graffiti/Disc 2`
    - `Physical Graffiti (disc 2)`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song 289 400, Germany)/disc2`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song SS 200-2, USA, WEA)/disc2`
    - `Physical Graffiti - Disc 2`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song 55XD661-2, Japan)/disc2`
    - `Physical Graffiti CD1`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song-BMG 92442-2, USA)/Disc 1`
    - `Physical Graffiti CD2`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song-BMG 92442-2, USA)/Disc 2`
    - `Physical Graffiti Disc 1`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song 7567-92442-2, Germany)/disc1`
    - `Physical Graffiti Disc 2`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song 7567-92442-2, Germany)/disc2`
    - `Physical Graffiti [CD1]`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song SS 200-2, USA, JVC)/disc1`
    - `Physical Graffiti cd1`
        - artist: Led Zeppelin  ·  year: 1975
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song WPCR-14848-9, Japan)/disc1`
    - `Physical Graffiti cd2`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Studio/1975. Physical Graffiti/1975. Led Zeppelin - Physical Graffiti (Swan Song WPCR-14848-9, Japan)/disc2`

### Piece Of Mind (7243 8 35871 2 4)  (4)
    - `Piece Of Mind (7243 8 35871 2 4, CD1)`
        - artist: Iron Maiden  ·  year: 1983
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1983 Piece Of Mind/1983 Piece Of Mind [1995 Holland 7243 8 35871 2 4 EMI]/CD 1`
    - `Piece Of Mind (7243 8 35871 2 4, CD2)`
        - artist: Iron Maiden  ·  year: 1983
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1983 Piece Of Mind/1983 Piece Of Mind [1995 Holland 7243 8 35871 2 4 EMI]/CD 2`
    - `Piece Of Mind [Castle 105-2] CD 1`
        - artist: Iron Maiden  ·  year: 1983
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1983 Piece Of Mind/1983 Piece Of Mind [1995 USA CASTLE 105-2. Castle Records]/CD 1`
    - `Piece Of Mind [Castle 105-2] CD 2`
        - artist: Iron Maiden  ·  year: 1995
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1983 Piece Of Mind/1983 Piece Of Mind [1995 USA CASTLE 105-2. Castle Records]/CD 2`

### Platinum Collection  (4)
    - `Platinum Collection CD3`
        - artist: Scorpions  ·  year: 2005
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Compilations/2005 The Platinum Collection/2005 The Platinum Collection [2005 Germany 00946 340 407 23 EMI]/CD 3`
    - `Platinum Collection [cd1]`
        - artist: Tina Turner  ·  year: 2009
        - folder: `D:/music/_cd_rip/Tina Turner/2009 - TT_The Platinum Collection_(3 CD)/CD1`
    - `Platinum Collection [cd2]`
        - artist: Tina Turner  ·  year: 2009
        - folder: `D:/music/_cd_rip/Tina Turner/2009 - TT_The Platinum Collection_(3 CD)/CD2`
    - `Platinum Collection [cd3]`
        - artist: Tina Turner  ·  year: 2009
        - folder: `D:/music/_cd_rip/Tina Turner/2009 - TT_The Platinum Collection_(3 CD)/CD3`

### Playlist + Plus  (3)
    - `Playlist + Plus (CD1)`
        - artist: Kiss  ·  year: 2008
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2008 Playlist + Plus [Germany Universal 06025 17491601]/CD 1`
    - `Playlist + Plus (CD2)`
        - artist: Kiss  ·  year: 2008
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2008 Playlist + Plus [Germany Universal 06025 17491601]/CD 2`
    - `Playlist + Plus (CD3)`
        - artist: Kiss  ·  year: 2008
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2008 Playlist + Plus [Germany Universal 06025 17491601]/CD 3`

### Powerslave (7243 8 35872 2 3)  (2)
    - `Powerslave (7243 8 35872 2 3, CD1)`
        - artist: Iron Maiden  ·  year: 1984
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1984 Powerslave/1984 Powerslave [1995 Holland 7243 8 35872 2 3 EMI]/CD 1`
    - `Powerslave (7243 8 35872 2 3, CD2)`
        - artist: Iron Maiden  ·  year: 1984
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1984 Powerslave/1984 Powerslave [1995 Holland 7243 8 35872 2 3 EMI]/CD 2`

### Priest... Live! (2001, Columbia, The Remasters Series, 502136 2, UK)  (4)
    - `Priest... Live! (2001, Columbia, The Remasters Series, 502136 2, UK, CD 1)`
        - artist: Judas Priest  ·  year: 2001
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1987 - Priest... Live!/2001 - Priest... Live! [Columbia, 502136 2, UK]`
    - `Priest... Live! (2001, Columbia, The Remasters Series, 502136 2, UK, CD 2)`
        - artist: Judas Priest  ·  year: 2001
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1987 - Priest... Live!/2001 - Priest... Live! [Columbia, 502136 2, UK]`
    - `Priest... Live! (2012, Sony, SICP 3401, Japan, CD 1)`
        - artist: Judas Priest  ·  year: 1987
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1987 - Priest... Live!/2012 - Priest... Live! [Sony, SICP 3401~2, Japan]`
    - `Priest... Live! (2012, Sony, SICP 3402, Japan, CD 2)`
        - artist: Judas Priest  ·  year: 1987
        - folder: `D:/music/_cd_rip/Judas Priest/Live Albums/1987 - Priest... Live!/2012 - Priest... Live! [Sony, SICP 3401~2, Japan]`

### Priest...Live! (2012, Sony / Columbia, 88697967872-JK12, USA)  (2)
    - `Priest...Live! (2012, Sony / Columbia, 88697967872-JK12, USA, CD 1)`
        - artist: Judas Priest  ·  year: 1987
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2012 - The Complete Albums Collection [19 CD Boxset, Sony-Legacy, 88697967872, USA]/1987 - Priest... Live! [Columbia, 88697967872-J12]/CD1`
    - `Priest...Live! (2012, Sony / Columbia, 88697967872-JK12, USA, CD 2)`
        - artist: Judas Priest  ·  year: 1987
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2012 - The Complete Albums Collection [19 CD Boxset, Sony-Legacy, 88697967872, USA]/1987 - Priest... Live! [Columbia, 88697967872-J12]/CD2`

### Prince of Darkness (E4K 92960)  (4)
    - `Prince of Darkness [CD 1] (E4K 92960)`
        - artist: Ozzy Osbourne  ·  year: 2005
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Boxsets/2005 Prince Of Darkness/2005 Prince Of Darkness [2005 USA E4K 92960 Epic]/CD 1`
    - `Prince of Darkness [CD 2] (E4K 92960)`
        - artist: Ozzy Osbourne  ·  year: 2005
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Boxsets/2005 Prince Of Darkness/2005 Prince Of Darkness [2005 USA E4K 92960 Epic]/CD 2`
    - `Prince of Darkness [CD 3] (E4K 92960)`
        - artist: Ozzy Osbourne  ·  year: 2005
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Boxsets/2005 Prince Of Darkness/2005 Prince Of Darkness [2005 USA E4K 92960 Epic]/CD 3`
    - `Prince of Darkness [CD 4] (E4K 92960)`
        - artist: Ozzy Osbourne  ·  year: 2005
        - folder: `D:/music/_cd_rip/Ozzy Osbourne-Дискография (1980-2020)/Boxsets/2005 Prince Of Darkness/2005 Prince Of Darkness [2005 USA E4K 92960 Epic]/CD 4`

### Private Investigations - The Best Of  (2)
    - `Private Investigations - The Best Of (CD1)`
        - artist: Dire Straits  ·  year: 2005
        - folder: `D:/music/_cd_rip/(1983-2018) Mark Knopfler - Дискография (95CD, 2 Digital Download)/Compilations/(2005) Private Investigations - The Best Of [Mercury, 9873051, Germany] [special edition]`
    - `Private Investigations - The Best Of (CD2)`
        - artist: Mark Knopfler, Dire Straits  ·  year: 2005
        - folder: `D:/music/_cd_rip/(1983-2018) Mark Knopfler - Дискография (95CD, 2 Digital Download)/Compilations/(2005) Private Investigations - The Best Of [Mercury, 9873051, Germany] [special edition]`

### Privateering  (3)
    - `Privateering (CD1)`
        - artist: Mark Knopfler  ·  year: 2012
        - folder: `D:/music/_cd_rip/(1983-2018) Mark Knopfler - Дискография (95CD, 2 Digital Download)/Albums/(2012) Privateering [Mercury, 3704323, Germany]`
    - `Privateering (CD2)`
        - artist: Mark Knopfler  ·  year: 2012
        - folder: `D:/music/_cd_rip/(1983-2018) Mark Knopfler - Дискография (95CD, 2 Digital Download)/Albums/(2012) Privateering [Mercury, 3704323, Germany]`
    - `Privateering (CD3)`
        - artist: Mark Knopfler  ·  year: 2012
        - folder: `D:/music/_cd_rip/(1983-2018) Mark Knopfler - Дискография (95CD, 2 Digital Download)/Albums/(2012) Privateering [Mercury, 3708118, Germany]`

### Psychedelic Pill  (2)
    - `Psychedelic Pill (CD2)`
        - artist: Neil Young & Crazy Horse  ·  year: 2012
        - folder: `D:/music/_cd_rip/Neil Young/2012. Neil Young & Crazy Horse - Psychedelic Pill (2012 Reprise Records, 9362-49485-9, E.U.)/CD 2`
    - `Psychedelic Pill Disc 1`
        - artist: Neil Young & Crazy Horse  ·  year: 2012
        - folder: `D:/music/_cd_rip/Neil Young/2012. Neil Young & Crazy Horse - Psychedelic Pill (2012 Reprise Records, 9362-49485-9, E.U.)/CD 1`

### Pulse (MHCP 689)  (5)
    - `Pulse (Disc 1) (MHCP 689)`
        - artist: Pink Floyd  ·  year: 1995
        - folder: `D:/music/Pink Floyd/Live/1995 Pulse/(Sony MHCP 689-90) [3rd Japan]/Disc 1`
    - `Pulse (Disc 2) (MHCP 690)`
        - artist: Pink Floyd  ·  year: 1995
        - folder: `D:/music/Pink Floyd/Live/1995 Pulse/(Sony MHCP 689-90) [3rd Japan]/Disc 2`
    - `Pulse (Live) CD2`
        - artist: Pink Floyd  ·  year: 1995
        - folder: `D:/music/Pink Floyd/Live/1995 Pulse/(PFR PFR17) [EU, 2016 issue]/CD2`
    - `Pulse (SRCS 7813 Japan 2nd press) CD 1`
        - artist: Pink Floyd  ·  year: 1995
        - folder: `D:/music/Pink Floyd/Live/1995 Pulse/(Sony SRCS 7813-14) [2nd Japan]/CD1`
    - `Pulse (SRCS 7814 Japan 2nd press) CD 2`
        - artist: Pink Floyd  ·  year: 1995
        - folder: `D:/music/Pink Floyd/Live/1995 Pulse/(Sony SRCS 7813-14) [2nd Japan]/CD2`

### Quadrophenia  (2)
    - `Quadrophenia (Disc 1)`
        - artist: The Who  ·  year: 1973
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1973 - Quadrophenia (2 CD MCA MCAD2-11463)/Disc 1`
    - `Quadrophenia (Disc 2)`
        - artist: The Who  ·  year: 1973
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1973 - Quadrophenia (2 CD MCA MCAD2-11463)/Disc 2`

### Rare & Unreleased  (3)
    - `Rare & Unreleased CD1`
        - artist: Nazareth  ·  year: 2018
        - folder: `D:/music/_cd_rip/Nazareth - 2018 - Loud & Proud! (32CD + 6LP + 3 x 7'' Vinyl Box Set Union Square Music)/2018 Rare & Unreleased 3CD/CD1`
    - `Rare & Unreleased CD2`
        - artist: Nazareth  ·  year: 2018
        - folder: `D:/music/_cd_rip/Nazareth - 2018 - Loud & Proud! (32CD + 6LP + 3 x 7'' Vinyl Box Set Union Square Music)/2018 Rare & Unreleased 3CD/CD2`
    - `Rare & Unreleased CD3`
        - artist: Nazareth  ·  year: 2018
        - folder: `D:/music/_cd_rip/Nazareth - 2018 - Loud & Proud! (32CD + 6LP + 3 x 7'' Vinyl Box Set Union Square Music)/2018 Rare & Unreleased 3CD/CD3`

### Rare Masters  (2)
    - `Rare Masters (CD1)`
        - artist: Elton John  ·  year: 1992
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/1992. Elton John - Rare Masters (Polydor 314 514 138-2, USA)/CD1`
    - `Rare Masters (CD2)`
        - artist: Elton John  ·  year: 1992
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/1992. Elton John - Rare Masters (Polydor 314 514 138-2, USA)/CD2`

### Re-Idolized  (2)
    - `Re-Idolized (Disc 1)`
        - artist: W.A.S.P.  ·  year: 2018
        - folder: `D:/music/W.A.S.P. - Re-Idolized (2018)/CD1`
    - `Re-Idolized (Disc 2)`
        - artist: W.A.S.P.  ·  year: 2018
        - folder: `D:/music/W.A.S.P. - Re-Idolized (2018)/CD2`

### Real (Remastered & Expanded Special Edition)  (2)
    - `Real (Remastered & Expanded Special Edition) CD 1`
        - artist: Belinda Carlisle  ·  year: 1993
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1993 Real (2013, Edsel Records, EDSG 8028)/CD1`
    - `Real (Remastered & Expanded Special Edition) CD 2`
        - artist: Belinda Carlisle  ·  year: 1993
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1993 Real (2013, Edsel Records, EDSG 8028)/CD2`

### Rebel Heart (Deluxe Version)  (2)
    - `Rebel Heart (Deluxe Version) Disc 1`
        - artist: Madonna  ·  year: 2015
        - folder: `D:/music/_cd_rip/Madonna - Discography/2015 - Rebel Heart (2 CD Deluxe Version) (2015 - Boy Toy, Inc. • Interscope Records • Live Nation Inc. - Germany - 0602547244116)/Disc One`
    - `Rebel Heart (Deluxe Version) Disc 2`
        - artist: Madonna  ·  year: 2015
        - folder: `D:/music/_cd_rip/Madonna - Discography/2015 - Rebel Heart (2 CD Deluxe Version) (2015 - Boy Toy, Inc. • Interscope Records • Live Nation Inc. - Germany - 0602547244116)/Disc Two`

### Red [2013, DGM5011~2]  (2)
    - `Red [2013, DGM5011~2] CD1`
        - artist: King Crimson  ·  year: 1974
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1974. Red/2013, USA, Discipline Global Mobile, DGM5011~2/CD 1`
    - `Red [2013, DGM5011~2] CD2`
        - artist: King Crimson  ·  year: 1974
        - folder: `D:/music/King Crimson/2.Studio (rem.)/1974. Red/2013, USA, Discipline Global Mobile, DGM5011~2/CD 2`

### Red, White & Crue  (2)
    - `Red, White & Crue (Disc 2)`
        - artist: Motley Crue  ·  year: 2005
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2005 - Red, White & Crue [Hip-O Rec., B0003909-02, USA]`
    - `Red, White & Crue CD1`
        - artist: Motley Crue  ·  year: 2005
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2005 - Red, White & Crue [Hip-O Rec., B0003909-02, USA]`

### Red, White & Crüe  (2)
    - `Red, White & Crüe - CD1`
        - artist: Mötley Crüe  ·  year: 2005
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2005 - Red, White & Crue [Universal, SET 2103379, Argentina]`
    - `Red, White & Crüe - CD2`
        - artist: Mötley Crüe  ·  year: 2005
        - folder: `D:/music/_cd_rip/Motley Crue/Live & Compilation/2005 - Red, White & Crue [Universal, SET 2103379, Argentina]`

### Redeemer Of Souls (2014, Sony, SICP 30616, Japan)  (2)
    - `Redeemer Of Souls (2014, Sony, SICP 30616, Japan, CD 1)`
        - artist: Judas Priest  ·  year: 2014
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/2014 - Redeemer Of Souls/2014 - Redeemer Of Souls [Sony, SICP 30616~7, Japan]`
    - `Redeemer Of Souls (2014, Sony, SICP 30617, Japan, CD 2)`
        - artist: Judas Priest  ·  year: 2014
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/2014 - Redeemer Of Souls/2014 - Redeemer Of Souls [Sony, SICP 30616~7, Japan]`

### Reflection  (3)
    - `Reflection (Disc 1)`
        - artist: Paul Mauriat  ·  year: 1997
        - folder: `D:/music/Paul Mauriat (FLAC)/1997. Reflection (3 CD)/Disc 1`
    - `Reflection (Disc 2)`
        - artist: Paul Mauriat  ·  year: 1997
        - folder: `D:/music/Paul Mauriat (FLAC)/1997. Reflection (3 CD)/Disc 2`
    - `Reflection (Disc 3)`
        - artist: Paul Mauriat  ·  year: 1997
        - folder: `D:/music/Paul Mauriat (FLAC)/1997. Reflection (3 CD)/Disc 3`

### Reidolized  (2)
    - `Reidolized - CD1`
        - artist: W.A.S.P.  ·  year: 2017
        - folder: `D:/music/W.A.S.P. - ReIdolized (The Soundtrack to the Crimson Idol) (2017)/CD1`
    - `Reidolized - CD2`
        - artist: W.A.S.P.  ·  year: 2017
        - folder: `D:/music/W.A.S.P. - ReIdolized (The Soundtrack to the Crimson Idol) (2017)/CD2`

### Remasters (7567-80415-2)  (4)
    - `Remasters (7567-80415-2) CD 1`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/1990. Led Zeppelin - Remasters (Atlantic 7567-80415-2, Germany)/Disc 1`
    - `Remasters (7567-80415-2) CD 2`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/1990. Led Zeppelin - Remasters (Atlantic 7567-80415-2, Germany)/Disc 2`
    - `Remasters (Disc 1)`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/1990. Led Zeppelin - Remasters (Atlantic AMCY 168-9, Japan)/Disc 1`
    - `Remasters (Disc 2)`
        - artist: Led Zeppelin  ·  year: 1990
        - folder: `D:/music/_cd_rip/Led Zeppelin/Compilations/1990. Led Zeppelin - Remasters (Atlantic AMCY 168-9, Japan)/Disc 2`

### Remixes 2. 81-11  (3)
    - `Remixes 2. 81-11 Disc 1`
        - artist: Depeche Mode  ·  year: 2011
        - folder: `D:/music/Depeche Mode/22. Depeche Mode - Remixes 2. 81 - 11 (2011)/Depeche Mode - Remixes 2. 81 - 11 (2011) (lcdmutel18 5099909663626) (EU 2011)/Disc 1`
    - `Remixes 2. 81-11 Disc 2`
        - artist: Depeche Mode  ·  year: 2011
        - folder: `D:/music/Depeche Mode/22. Depeche Mode - Remixes 2. 81 - 11 (2011)/Depeche Mode - Remixes 2. 81 - 11 (2011) (lcdmutel18 5099909663626) (EU 2011)/Disc 2`
    - `Remixes 2. 81-11 Disc 3`
        - artist: Depeche Mode  ·  year: 2011
        - folder: `D:/music/Depeche Mode/22. Depeche Mode - Remixes 2. 81 - 11 (2011)/Depeche Mode - Remixes 2. 81 - 11 (2011) (lcdmutel18 5099909663626) (EU 2011)/Disc 3`

### Remixes 81...04  (3)
    - `Remixes 81...04 Disc 1`
        - artist: Depeche Mode  ·  year: 2004
        - folder: `D:/music/Depeche Mode/18. Depeche Mode - Remixes 81...04 (2004)/Depeche Mode - Remixes 81...04 (2004) (XLCDMUTEL8 0724387455924) (EU 2004)/Disc 1`
    - `Remixes 81...04 Disc 2`
        - artist: Depeche Mode  ·  year: 2004
        - folder: `D:/music/Depeche Mode/18. Depeche Mode - Remixes 81...04 (2004)/Depeche Mode - Remixes 81...04 (2004) (XLCDMUTEL8 0724387455924) (EU 2004)/Disc 2`
    - `Remixes 81...04 Disc 3`
        - artist: Depeche Mode  ·  year: 2004
        - folder: `D:/music/Depeche Mode/18. Depeche Mode - Remixes 81...04 (2004)/Depeche Mode - Remixes 81...04 (2004) (XLCDMUTEL8 0724387455924) (EU 2004)/Disc 3`

### rendez-vous  (2)
    - `rendez-vous CD1`
        - artist: Kaas, Patricia  ·  year: 1998
        - folder: `D:/music/Patrisia Kaas/Rendez-Vous/CD1`
    - `rendez-vous CD2`
        - artist: Kaas, Patricia  ·  year: 1998
        - folder: `D:/music/Patrisia Kaas/Rendez-Vous/CD2`

### Reunion  (4)
    - `Reunion (CD1)`
        - artist: Black Sabbath  ·  year: 1998
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1998 Reunion/1998 Reunion [1998 USA E2K 69115 Epic  Sony]/CD1`
    - `Reunion (CD2)`
        - artist: Black Sabbath  ·  year: 1998
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1998 Reunion/1998 Reunion [1998 USA E2K 69115 Epic  Sony]/CD2`
    - `Reunion (Japanese SRCS 8807~8, CD1)`
        - artist: Black Sabbath  ·  year: 1998
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1998 Reunion/1998 Reunion [1998 Japan SRCS 8807~8 Sony]/CD1`
    - `Reunion (Japanese SRCS 8807~8, CD2)`
        - artist: Black Sabbath  ·  year: 1998
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Live Albums/1998 Reunion/1998 Reunion [1998 Japan SRCS 8807~8 Sony]/CD2`

### Revolver (MFSL SuperVinyl 24/96)  (2)
    - `Revolver  (MFSL SuperVinyl 24/96)`
        - artist: The Beatles  ·  year: 1966
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/07. 1966 - Revolver {24bit.96kHz Vinyl Rip MFSL 1-107}`
    - `Revolver (Disc 1) (00602445382774)`
        - artist: The Beatles  ·  year: 2022
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2022 - Revolver/Revolver (2CD Edition)/Disc 1`

### Rock The Night - The Very Best Of Europe  (2)
    - `Rock The Night - The Very Best Of Europe CD1`
        - artist: Europe  ·  year: 2004
        - folder: `D:/music/_cd_rip/EUROPE - Discorgaphy (lossless)/Compilations/2004-03-03 - Rock The Night - The Very Best Of Europe (2CD) [516054 2, 2004] {bdg63}`
    - `Rock The Night - The Very Best Of Europe CD2`
        - artist: Europe  ·  year: 2004
        - folder: `D:/music/_cd_rip/EUROPE - Discorgaphy (lossless)/Compilations/2004-03-03 - Rock The Night - The Very Best Of Europe (2CD) [516054 2, 2004] {bdg63}`

### Rock'n'Roll (2006, USA, Santuary, 06076-86419-2)  (2)
    - `Rock'n'Roll (2006, USA, Santuary, 06076-86419-2, CD1)`
        - artist: Motorhead  ·  year: 1987
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1987-Rock'n'Roll/1987-Rock'N'Roll (2006, USA, Santuary, 06076-86419-2, 2CD)/CD1`
    - `Rock'n'Roll (2006, USA, Santuary, 06076-86419-2, CD2)`
        - artist: Motorhead  ·  year: 1987
        - folder: `D:/music/_cd_rip/Motorhead/Full Length Albums/1987-Rock'n'Roll/1987-Rock'N'Roll (2006, USA, Santuary, 06076-86419-2, 2CD)/CD2`

### Rocks Donington 2014 [GQXS-90031-4]  (2)
    - `Rocks Donington 2014 [GQXS-90031-4] CD1`
        - artist: Aerosmith  ·  year: 2015
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Lives/2015 - Rocks Donington 2014/2015 - Rocks Donington 2014 (GQXS-90031-4) Japan/CD-1`
    - `Rocks Donington 2014 [GQXS-90031-4] CD2`
        - artist: Aerosmith  ·  year: 2015
        - folder: `D:/music/_cd_rip/Aerosmith - Discography/Lives/2015 - Rocks Donington 2014/2015 - Rocks Donington 2014 (GQXS-90031-4) Japan/CD-2`

### Runaway Horses (Remastered & Expanded Special Edition)  (2)
    - `Runaway Horses (Remastered & Expanded Special Edition) CD 1`
        - artist: Belinda Carlisle  ·  year: 1989
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1989 Runaway Horses (2013, Edsel Records, EDSG 8026)/CD1`
    - `Runaway Horses (Remastered & Expanded Special Edition) CD 2`
        - artist: Belinda Carlisle  ·  year: 1989
        - folder: `D:/music/_cd_rip/Belinda Carlisle 2013-2014 Remasters/1989 Runaway Horses (2013, Edsel Records, EDSG 8026)/CD2`

### S&M [Germany Vertigo 546 797-2]  (10)
    - `S&M (Disc 1) [Germany Vertigo 546 797-2]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/1999 Germany Vertigo 546 797-2/Disc 1`
    - `S&M (Disc 1) [SHM-CD, UICY-94672]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/2010 Japan SHM-CD, UICY-94672~3/Disc 1`
    - `S&M (Disc 1) [SRCS 2144]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/1999 Japan SRCS 2144~5/Disc 1`
    - `S&M (Disc 1) [UICR-1062]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/2006 Japan UICR-1062~3/Disc 1`
    - `S&M (Disc 1) [USA Elektra 62463-2]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/1999 USA Promo CD, Elektra 62463-2/Disc 1`
    - `S&M (Disc 2) [Germany Vertigo 546 797-2]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/1999 Germany Vertigo 546 797-2/Disc 2`
    - `S&M (Disc 2) [SHM-CD, UICY-94673]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/2010 Japan SHM-CD, UICY-94672~3/Disc 2`
    - `S&M (Disc 2) [SRCS 2145]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/1999 Japan SRCS 2144~5/Disc 2`
    - `S&M (Disc 2) [UICR-1063]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/2006 Japan UICR-1062~3/Disc 2`
    - `S&M (Disc 2) [USA Elektra 62463-2]`
        - artist: Metallica  ·  year: 1999
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/1999 - S&M (2 CD)/1999 USA Promo CD, Elektra 62463-2/Disc 2`

### Sacred Heart (2012, Deluxe Expanded Edition)  (2)
    - `Sacred Heart (2012, Deluxe Expanded Edition, Disc 1)`
        - artist: Dio  ·  year: 1985
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1985 Sacred Heart/1985 Sacred Heart [2012 Universal 2776762 Germany]/Disc 1 - Sacret Heart`
    - `Sacred Heart (2012, Deluxe Expanded Edition, Disc 2)`
        - artist: Dio  ·  year: 1985
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1985 Sacred Heart/1985 Sacred Heart [2012 Universal 2776762 Germany]/Disc 2 - Bonus Tracks`

### Saving The Best To Last. . .  (2)
    - `Saving The Best To Last. . . (Disc 1)`
        - artist: James Last  ·  year: 2005
        - folder: `D:/music/James Last, flac/2005 - Saving the best to Last (2 CD)/CD1`
    - `Saving The Best To Last. . . (Disc 2)`
        - artist: James Last  ·  year: 2005
        - folder: `D:/music/James Last, flac/2005 - Saving the best to Last (2 CD)/CD2`

### Scandinavian Nights (1988, 2CD, DP VSOP CD125)  (2)
    - `Scandinavian Nights CD1 (1988, 2CD, DP VSOP CD125)`
        - artist: Deep Purple  ·  year: 1988
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1988. Scandinavian Nights (1988, 2CD, Connolsseur, UK, DP VSOP CD125)/CD1`
    - `Scandinavian Nights CD2 (1988, 2CD, DP VSOP CD125)`
        - artist: Deep Purple  ·  year: 1988
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/1988. Scandinavian Nights (1988, 2CD, Connolsseur, UK, DP VSOP CD125)/CD2`

### Score: 20th Anniversary World Tour Live with The Octavarium Orchestra  (3)
    - `Score: 20th Anniversary World Tour Live with The Octavarium Orchestra CD 1`
        - artist: Dream Theater  ·  year: 2006
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2006-Score`
    - `Score: 20th Anniversary World Tour Live with The Octavarium Orchestra CD 2`
        - artist: Dream Theater  ·  year: 2006
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2006-Score`
    - `Score: 20th Anniversary World Tour Live with The Octavarium Orchestra CD 3`
        - artist: Dream Theater  ·  year: 2006
        - folder: `D:/music/_cd_rip/Dream Theater/Live Albums/2006-Score`

### Scoundrel Days (Deluxe Edition)  (2)
    - `Scoundrel Days (Deluxe Edition) (Disc 1)`
        - artist: a-ha  ·  year: 2010
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Scoundrel Days (Remastered & Expanded) 2CD/CD1`
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Scoundrel Days (Deluxe Edition) (Japan 2CD)/CD1`
    - `Scoundrel Days (Deluxe Edition) (Disc 2)`
        - artist: a-ha  ·  year: 2010
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Scoundrel Days (Remastered & Expanded) 2CD/CD2`
        - folder: `D:/music/A-ha - Full Albums Discography/A-ha - 2010 - Scoundrel Days (Deluxe Edition) (Japan 2CD)/CD2`

### Secret World Live (PGCD 8)  (2)
    - `Secret World Live (PGCD 8) - CD1`
        - artist: Peter Gabriel  ·  year: 1994
        - folder: `D:/music/PETER GABRIEL - DISCOGRAPHY/(1994) Secret World Live (PGCD 8)/CD1`
    - `Secret World Live (PGCD 8) - CD2`
        - artist: Peter Gabriel  ·  year: 1994
        - folder: `D:/music/PETER GABRIEL - DISCOGRAPHY/(1994) Secret World Live (PGCD 8)/CD2`

### Seine schцnsten Melodien  (2)
    - `Seine schцnsten Melodien (CD1)`
        - artist: James Last  ·  year: 2007
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2007 - Seine schönsten Melodien [Universal Music 06007 5300980 2] 2CD/CD1`
    - `Seine schцnsten Melodien (CD2)`
        - artist: James Last  ·  year: 2007
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2007 - Seine schönsten Melodien [Universal Music 06007 5300980 2] 2CD/CD2`

### Seventh Son Of A Seventh Son (7243 8 35875 2 0)  (4)
    - `Seventh Son Of A Seventh Son (7243 8 35875 2 0, CD1)`
        - artist: Iron Maiden  ·  year: 1988
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1988 Seventh Son Of A Seventh Son/1988 Seventh Son Of A Seventh Son [1995 Holland 7243 8 35875 2 0 EMI]/CD 1`
    - `Seventh Son Of A Seventh Son (7243 8 35875 2 0, CD2)`
        - artist: Iron Maiden  ·  year: 1988
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1988 Seventh Son Of A Seventh Son/1988 Seventh Son Of A Seventh Son [1995 Holland 7243 8 35875 2 0 EMI]/CD 2`
    - `Seventh Son Of A Seventh Son [Castle 109-2] CD 1`
        - artist: Iron Maiden  ·  year: 1988
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1988 Seventh Son Of A Seventh Son/1988 Seventh Son Of A Seventh Son [1995 USA  CASTLE 109-2. Castle Records]/CD 1`
    - `Seventh Son Of A Seventh Son [Castle 109-2] CD 2`
        - artist: Iron Maiden  ·  year: 1988
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1988 Seventh Son Of A Seventh Son/1988 Seventh Son Of A Seventh Son [1995 USA  CASTLE 109-2. Castle Records]/CD 2`

### Seventh Star (2010, Sanctuary, Deluxe Edition, 2752472, Germany)  (2)
    - `Seventh Star (2010, Sanctuary, Deluxe Edition, 2752472, CD1, Germany)`
        - artist: Black Sabbath  ·  year: 1986
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1986 Seventh Star/1986 Seventh Star [2010 Germany 2752472 Sanctuary]/CD - 1`
    - `Seventh Star (2010, Sanctuary, Deluxe Edition, 2752472, CD2, Germany)`
        - artist: Black Sabbath  ·  year: 1986
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Studio Albums/1986 Seventh Star/1986 Seventh Star [2010 Germany 2752472 Sanctuary]/CD - 2`

### Sgt. Pepper  (4)
    - `Sgt. Pepper CD1`
        - artist: The Beatles  ·  year: 1967
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2017 - Sgt. Pepper’s LHCB (Apple 0602557455328)/Disc 1 New Stereo Remix`
    - `Sgt. Pepper CD2`
        - artist: The Beatles  ·  year: 1967
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2017 - Sgt. Pepper’s LHCB (Apple 0602557455328)/Disc 2 Sgt. Pepper Sessions`
    - `Sgt. Pepper CD3`
        - artist: The Beatles  ·  year: 1967
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2017 - Sgt. Pepper’s LHCB (Apple 0602557455328)/Disc 3 Sgt. Pepper Sessions`
    - `Sgt. Pepper CD4`
        - artist: The Beatles  ·  year: 1967
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2017 - Sgt. Pepper’s LHCB (Apple 0602557455328)/Disc 4 Sgt. Pepper In Mono`

### Shine A Light  (4)
    - `Shine A Light (CD1)`
        - artist: The Rolling Stones, The Rolling Stones feat. Jack White III, The Rolling Stones feat. Buddy Guy  ·  year: 2008
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2008 Shine A Light/Shine A Light [UICY-1408-9]/CD1`
    - `Shine A Light (Disc 1) (1764747)`
        - artist: The Rolling Stones  ·  year: 2008
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2008 Shine A Light/Shine A Light [Polydor 1764747]/Disc 1`
    - `Shine A Light (Disc 2) (1764747)`
        - artist: The Rolling Stones  ·  year: 2008
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2008 Shine A Light/Shine A Light [Polydor 1764747]/Disc 2`
    - `Shine A Light [CD2]`
        - artist: The Rolling Stones  ·  year: 2008
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2008 Shine A Light/Shine A Light [UICY-1408-9]/CD2`

### Showtime, Storytime  (2)
    - `Showtime, Storytime (CD1)`
        - artist: Nightwish  ·  year: 2013
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Live Albums/2013 Showtime, Storytime/2013 Showtime, Storytime [2013 Nuclear Blast NB 3206-4 Germany]/CD 1`
    - `Showtime, Storytime (CD2)`
        - artist: Nightwish  ·  year: 2013
        - folder: `D:/music/_cd_rip/Nightwish (1997-2015)/Live Albums/2013 Showtime, Storytime/2013 Showtime, Storytime [2013 Nuclear Blast NB 3206-4 Germany]/CD 2`

### Signify  (2)
    - `Signify CD1`
        - artist: Porcupine Tree  ·  year: 1996
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1996 - Signify 2CD (Remaster 2003) (Kscope KSCOPE131 China)/CD1`
    - `Signify CD2`
        - artist: Porcupine Tree  ·  year: 1996
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1996 - Signify 2CD (Remaster 2003) (Kscope KSCOPE131 China)/CD2`

### Single Cuts - - Evening Star  (2)
    - `Single Cuts - CD 03 - Evening Star`
        - artist: Judas Priest  ·  year: 1978
        - folder: `D:/music/_cd_rip/Judas Priest/Singles & EPs/2011 - Single Cuts [20 CD Boxset, Sony, 88697968832, Germany]/CD 03 - Evening Star`
    - `Single Cuts - CD 06 - Evening Star`
        - artist: Judas Priest  ·  year: 1979
        - folder: `D:/music/_cd_rip/Judas Priest/Singles & EPs/2011 - Single Cuts [20 CD Boxset, Sony, 88697968832, Germany]/CD 06 - Evening Star`

### Singles Collection  (2)
    - `Singles Collection CD 1`
        - artist: Lady Gaga  ·  year: 2017
        - folder: `D:/music/2017 Lady Gaga  - Singles Collection (2 CD) flac/CD 1`
    - `Singles Collection CD 2`
        - artist: Lady Gaga  ·  year: 2017
        - folder: `D:/music/2017 Lady Gaga  - Singles Collection (2 CD) flac/CD 2`

### Singles Collection - The London Years  (3)
    - `Singles Collection - The London Years (CD1)`
        - artist: The Rolling Stones  ·  year: 1995
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1989 Singles Collection - The London Years/Singles Collection - The London Years [ABKCO 844481-2]/Disc 1`
    - `Singles Collection - The London Years (CD2)`
        - artist: The Rolling Stones  ·  year: 1995
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1989 Singles Collection - The London Years/Singles Collection - The London Years [ABKCO 844481-2]/Disc 2`
    - `Singles Collection - The London Years (CD3)`
        - artist: The Rolling Stones  ·  year: 1995
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1989 Singles Collection - The London Years/Singles Collection - The London Years [ABKCO 844481-2]/Disc 3`

### Singles Collection: The London Years 882340-2  (2)
    - `Singles Collection: The London Years (Disc1)  882340-2`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1989 Singles Collection - The London Years/Singles Collection - The London Years [ABKCO 882340-2]/Disc 1`
    - `Singles Collection: The London Years (Disc3) 882340-2`
        - artist: The Rolling Stones  ·  year: 2002
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/3 Compilations/1989 Singles Collection - The London Years/Singles Collection - The London Years [ABKCO 882340-2]/Disc 3`

### Singles, EPs, B-Sides & Bonus Tracks  (3)
    - `Singles, EPs, B-Sides & Bonus Tracks CD1`
        - artist: Nazareth  ·  year: 2018
        - folder: `D:/music/_cd_rip/Nazareth - 2018 - Loud & Proud! (32CD + 6LP + 3 x 7'' Vinyl Box Set Union Square Music)/2018 Singles, EPs, B-Sides & Bonus Tracks 3CD/CD1`
    - `Singles, EPs, B-Sides & Bonus Tracks CD2`
        - artist: Nazareth  ·  year: 2018
        - folder: `D:/music/_cd_rip/Nazareth - 2018 - Loud & Proud! (32CD + 6LP + 3 x 7'' Vinyl Box Set Union Square Music)/2018 Singles, EPs, B-Sides & Bonus Tracks 3CD/CD2`
    - `Singles, EPs, B-Sides & Bonus Tracks CD3`
        - artist: Nazareth  ·  year: 2018
        - folder: `D:/music/_cd_rip/Nazareth - 2018 - Loud & Proud! (32CD + 6LP + 3 x 7'' Vinyl Box Set Union Square Music)/2018 Singles, EPs, B-Sides & Bonus Tracks 3CD/CD3`

### Singles, Plus  (2)
    - `Singles, Plus CD1`
        - artist: Johnny Cash  ·  year: 2012
        - folder: `D:/music/Cash - Collection/2012. Johnny Cash - The Complete Columbia Album Collection (Columbia 88697910472, EU)/62. Singles, Plus (disc 1) (2012)`
    - `Singles, Plus CD2`
        - artist: Johnny Cash  ·  year: 2012
        - folder: `D:/music/Cash - Collection/2012. Johnny Cash - The Complete Columbia Album Collection (Columbia 88697910472, EU)/63. Singles, Plus (disc 2) (2012)`

### Six Degrees Of Inner Turbulence  (2)
    - `Six Degrees Of Inner Turbulence CD 1`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Albums/2002-Six Degrees Of Inner Turbulence`
    - `Six Degrees Of Inner Turbulence CD 2`
        - artist: Dream Theater  ·  year: 2002
        - folder: `D:/music/_cd_rip/Dream Theater/Albums/2002-Six Degrees Of Inner Turbulence`

### so8os Present Sandra  (2)
    - `so8os Present Sandra (Disc 1)`
        - artist: Sandra  ·  year: 2012
        - folder: `D:/music/Sandra/2012 - so8os Presents Sandra (2012 - Germany - 440 850 2)/CD1`
    - `so8os Present Sandra (Disc 2)`
        - artist: Sandra  ·  year: 2012
        - folder: `D:/music/Sandra/2012 - so8os Presents Sandra (2012 - Germany - 440 850 2)/CD2`

### Sometime In New York City (EMI 5099990761928)  (4)
    - `Sometime In New York City (Disc 1) (EMI 5099990761928)`
        - artist: John & Yoko - Plastic Ono Band  ·  year: 2010
        - folder: `D:/music/_cd_rip/1969 - 2010 John Lennon/2010 John Lennon Signature Box Set/03 Sometime In New York City/Disc 1`
    - `Sometime In New York City (Disc 2) (EMI 5099990762024)`
        - artist: John & Yoko - Plastic Ono Band  ·  year: 2010
        - folder: `D:/music/_cd_rip/1969 - 2010 John Lennon/2010 John Lennon Signature Box Set/03 Sometime In New York City/Disc 2`
    - `Sometime in New York City (Live Jam) Disc 2`
        - artist: John Lennon  ·  year: 1972
        - folder: `D:/music/_cd_rip/1969 - 2010 John Lennon/1972 Sometime In New York City/Sometime In New York City (C2 93850)/Disc 2`
    - `Sometime in New York City Disc 1`
        - artist: John Lennon  ·  year: 1972
        - folder: `D:/music/_cd_rip/1969 - 2010 John Lennon/1972 Sometime In New York City/Sometime In New York City (C2 93850)/Disc 1`
        - folder: `D:/music/_cd_rip/1969 - 2010 John Lennon/1972 Sometime In New York City/Sometime In New York City (CDS 7 46782 8)/Disc 1`

### Somewhere In Time (7243 8 35874 2 1)  (4)
    - `Somewhere In Time (7243 8 35874 2 1, CD1)`
        - artist: Iron Maiden  ·  year: 1986
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1986 Somewhere In Time/1986 Somewhere In Time [1995 Holland 7243 8 35874 2 1 EMI]/CD 1`
    - `Somewhere In Time (7243 8 35874 2 1, CD2)`
        - artist: Iron Maiden  ·  year: 1986
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1986 Somewhere In Time/1986 Somewhere In Time [1995 Holland 7243 8 35874 2 1 EMI]/CD 2`
    - `Somewhere In Time [Castle 108-2] CD 1`
        - artist: Iron Maiden  ·  year: 1986
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1986 Somewhere In Time/1986 Somewhere In Time [1995 USA CASTLE 108-2. Castle Records]/CD 1`
    - `Somewhere In Time [Castle 108-2] CD 2`
        - artist: Iron Maiden  ·  year: 1986
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1986 Somewhere In Time/1986 Somewhere In Time [1995 USA CASTLE 108-2. Castle Records]/CD 2`

### Songs Of Innocence  (3)
    - `Songs Of Innocence (CD2)`
        - artist: U2  ·  year: 2014
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Albums/2014 - Songs Of Innocence [EU, Island 4704894] (2CD) {Deluxe Edition}/CD2`
    - `Songs Of Innocence [Disc 1]`
        - artist: U2  ·  year: 2014
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Albums/2014 - Songs Of Innocence [Japan, Island UICI-1134, 1135] (2CD) {Deluxe Edition}/CD1`
    - `Songs Of Innocence [Disc 2]`
        - artist: U2  ·  year: 2014
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Albums/2014 - Songs Of Innocence [Japan, Island UICI-1134, 1135] (2CD) {Deluxe Edition}/CD2`

### Spielen gemeinsam groЯe Welterfolge  (2)
    - `Spielen gemeinsam groЯe Welterfolge - CD 1`
        - artist: Richard Clayderman & James Last  ·  year: 2009
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2009 - Spielen gemeinsam große Welterfolge [Weltbild Harmony 703419] 2CD/CD1`
    - `Spielen gemeinsam groЯe Welterfolge - CD 2`
        - artist: Richard Clayderman & James Last  ·  year: 2009
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2009 - Spielen gemeinsam große Welterfolge [Weltbild Harmony 703419] 2CD/CD2`

### St. Anger (Australia Of A 2CD set)  (2)
    - `St. Anger (Australia CD1 Of A 2CD set)`
        - artist: Metallica  ·  year: 2003
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/2003 - St. Anger/2003 Australia 2CD Set/Disc 1, Universal 9865409`
    - `St. Anger (Australia CD2 Of A 2CD set)`
        - artist: Metallica  ·  year: 2003
        - folder: `D:/music/_cd_rip/Metallica - Discography/CD singles, EP, Promo CD/2003 - St. Anger/2003 Australia 2CD Set/Disc 2, Universal 9865410`

### Starfish On The Beach  (2)
    - `Starfish On The Beach Disc 1`
        - artist: Terry Jacks  ·  year: 2015
        - folder: `D:/music/Terry Jacks-Starfish on the Beach-2015/CD1`
    - `Starfish On The Beach Disc 2`
        - artist: Terry Jacks  ·  year: 2015
        - folder: `D:/music/Terry Jacks-Starfish on the Beach-2015/CD2`

### Stay Young 1979-1982: The Complete 'Deluxe Years'  (2)
    - `Stay Young 1979-1982: The Complete 'Deluxe Years' (Disc 1)`
        - artist: INXS  ·  year: 2002
        - folder: `D:/music/_cd_rip/INXS - коллекция/Compilations/2002. Stay Young 1979-1982 {2002, Australia, Raven RVCD-145}/CD1`
    - `Stay Young 1979-1982: The Complete 'Deluxe Years' (Disc 2)`
        - artist: INXS  ·  year: 2002
        - folder: `D:/music/_cd_rip/INXS - коллекция/Compilations/2002. Stay Young 1979-1982 {2002, Australia, Raven RVCD-145}/CD2`

### Staying A Life (ND 74720)  (4)
    - `Staying A Life (ND 74720) CD 1`
        - artist: Accept  ·  year: 1990
        - folder: `D:/music/_cd_rip/Accept - Discography/Live Albums/1990 Staying A Life/1990 Staying A Life [1990 RCA ND 74720 Germany]/CD 1`
    - `Staying A Life (ND 74720) CD 2`
        - artist: Accept  ·  year: 1990
        - folder: `D:/music/_cd_rip/Accept - Discography/Live Albums/1990 Staying A Life/1990 Staying A Life [1990 RCA ND 74720 Germany]/CD 2`
    - `Staying A Life [CD 1] [Japan, Sony Music, MHCP 791~2]`
        - artist: Accept  ·  year: 1990
        - folder: `D:/music/_cd_rip/Accept - Discography/Live Albums/1990 Staying A Life/1990 Staying A Life [2005 Epic MHCP-791~2 Japan]/CD 1`
    - `Staying A Life [CD 2] [Japan, Sony Music, MHCP 791~2]`
        - artist: Accept  ·  year: 1990
        - folder: `D:/music/_cd_rip/Accept - Discography/Live Albums/1990 Staying A Life/1990 Staying A Life [2005 Epic MHCP-791~2 Japan]/CD 2`

### Steelhammer - Live From Moscow  (2)
    - `Steelhammer - Live From Moscow (CD1)`
        - artist: U.D.O.  ·  year: 2014
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2014 Steelhammer - Live From Moscow -/2014 Steelhammer - Live From Moscow - [2014 AFM 502-7 Germany]/CD1`
    - `Steelhammer - Live From Moscow (CD2)`
        - artist: U.D.O.  ·  year: 2014
        - folder: `D:/music/_cd_rip/U.D.O. (1987-2015)/Live Albums/2014 Steelhammer - Live From Moscow -/2014 Steelhammer - Live From Moscow - [2014 AFM 502-7 Germany]/CD2`

### Sticky Fingers (Virgin CDV 2730)  (4)
    - `Sticky Fingers  (Virgin CDV 2730)`
        - artist: The Rolling Stones  ·  year: 1994
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/13. 1971 Sticky Fingers/Sticky Fingers [Virgin CDV 2730]`
    - `Sticky Fingers CD1`
        - artist: The Rolling Stones  ·  year: 1971
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/13. 1971 Sticky Fingers/Sticky Fingers [Universal 376 484 2]/Disc 1`
    - `Sticky Fingers CD2`
        - artist: The Rolling Stones  ·  year: 2015
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/13. 1971 Sticky Fingers/Sticky Fingers [Universal 376 484 2]/Disc 2`
    - `Sticky Fingers CD3`
        - artist: The Rolling Stones  ·  year: 1971
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/1 Studio/13. 1971 Sticky Fingers/Sticky Fingers [Universal 376 484 2]/Disc 3`

### Stiff Upper Lip (Australian Tour Edition)  (2)
    - `Stiff Upper Lip (Australian Tour Edition) Disc 2`
        - artist: AC/DC  ·  year: 2000
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Albums/2000 - Stiff Upper Lip/2000 Australian Tour Edition, Albert 7243 5 31019 0 9/CD 2`
    - `Stiff Upper Lip (CD single)`
        - artist: AC/DC  ·  year: 2000
        - folder: `D:/music/_cd_rip/AC-DC - Discography/Singles & Promos/2000 - Stiff Upper Lip (Elektra 7559-67041-2)`

### Strings of Peace  (2)
    - `Strings of Peace (Disc 1)`
        - artist: Paul Mauriat  ·  year: 2013
        - folder: `D:/music/Paul Mauriat (FLAC)/2013. Strings of Peace (2 CD)/Disc 1`
    - `Strings of Peace (Disc 2)`
        - artist: Paul Mauriat  ·  year: 2013
        - folder: `D:/music/Paul Mauriat (FLAC)/2013. Strings of Peace (2 CD)/Disc 2`

### Sweet Summer Sun - Hyde Park Live  (2)
    - `Sweet Summer Sun - Hyde Park Live (CD1)`
        - artist: The Rolling Stones  ·  year: 2013
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2013 Sweet Summer Sun [Promogracht EAGDV027]/Disc 1`
    - `Sweet Summer Sun - Hyde Park Live (CD2)`
        - artist: The Rolling Stones  ·  year: 2013
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2013 Sweet Summer Sun [Promogracht EAGDV027]/Disc 2`

### Symphonicities.  (2)
    - `Symphonicities. Disc 1`
        - artist: Sting  ·  year: 2010
        - folder: `D:/music/Sting - Symphonicities (2010) (Deutsche Grammophon 274 5361)/Disc 1`
    - `Symphonicities. Disc 2`
        - artist: Sting  ·  year: 2010
        - folder: `D:/music/Sting - Symphonicities (2010) (Deutsche Grammophon 274 5361)/Disc 2`

### Take No Prisoners  (2)
    - `Take No Prisoners - Disc 1`
        - artist: Lou Reed  ·  year: 1978
        - folder: `D:/music/_cd_rip/Lou Reed - Discography - FLAC/LIVE/1978 Live Take No Prisoners/cd 1`
    - `Take No Prisoners - Disk 2`
        - artist: Lou Reed  ·  year: 1978
        - folder: `D:/music/_cd_rip/Lou Reed - Discography - FLAC/LIVE/1978 Live Take No Prisoners/cd 2`

### Tear Ya Down: The Rarities (Castle, CMDDD444)  (2)
    - `Tear Ya Down: The Rarities (Castle, CMDDD444) (CD1)`
        - artist: Motorhead  ·  year: 2002
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2002-Tear Ya Down - The Rarities (2002, UK, Castle, CMDDD444, 2CD)/CD1`
    - `Tear Ya Down: The Rarities (Castle, CMDDD444) (CD2)`
        - artist: Motorhead  ·  year: 2002
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2002-Tear Ya Down - The Rarities (2002, UK, Castle, CMDDD444, 2CD)/CD2`

### Teases & Dares  (2)
    - `Teases & Dares (CD 1)`
        - artist: Kim Wilde  ·  year: 2010
        - folder: `D:/music/Kim Wilde discography/Kim Wilde 1984 ''Teases And Dares'' (Cherry Pop, 2010, 2CD)/CD 1`
    - `Teases & Dares (CD 2)`
        - artist: Kim Wilde  ·  year: 2010
        - folder: `D:/music/Kim Wilde discography/Kim Wilde 1984 ''Teases And Dares'' (Cherry Pop, 2010, 2CD)/CD 2`

### The Anthology  (5)
    - `The Anthology CD1`
        - artist: Nazareth  ·  year: 2009
        - folder: `D:/music/_cd_rip/Nazareth discography/Compilations/2009. Nazareth - The Anthology/2009. Nazareth - The Anthology (Salvo, EU, UK, SALVODCD210)/CD1`
    - `The Anthology CD1 (2016, 3CD, BMGCAT3CD4)`
        - artist: Emerson, Lake & Palmer  ·  year: 2016
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2016. Emerson, Lake & Palmer - The Anthology (2016, 3CD, BMG, UK-Austria, BMGCAT3CD4)/CD1`
    - `The Anthology CD2`
        - artist: Nazareth  ·  year: 2009
        - folder: `D:/music/_cd_rip/Nazareth discography/Compilations/2009. Nazareth - The Anthology/2009. Nazareth - The Anthology (Salvo, EU, UK, SALVODCD210)/CD2`
    - `The Anthology CD2 (2016, 3CD, BMGCAT3CD4)`
        - artist: Emerson, Lake & Palmer  ·  year: 2016
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2016. Emerson, Lake & Palmer - The Anthology (2016, 3CD, BMG, UK-Austria, BMGCAT3CD4)/CD2`
    - `The Anthology CD3 (2016, 3CD, BMGCAT3CD4)`
        - artist: Emerson, Lake & Palmer  ·  year: 2016
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2016. Emerson, Lake & Palmer - The Anthology (2016, 3CD, BMG, UK-Austria, BMGCAT3CD4)/CD3`

### The Anthology... So Far  (2)
    - `The Anthology... So Far (Disc 2)`
        - artist: Ringo Starr and his All Starr Band  ·  year: 2000
        - folder: `D:/music/_cd_rip/Ringo Starr - 1970 - 2019/2001 The Anthology...So Far (Eagle EDGCD484)/Disc 2`
        - folder: `D:/music/_cd_rip/Ringo Starr - 1970 - 2019/2001 The Anthology...So Far (Koch KOC-CD-8312)/Disc 2`
    - `The Anthology... So Far - Disc 1`
        - artist: Ringo, Nils Lofgren, Joe Walsh  ·  year: 2002
        - folder: `D:/music/_cd_rip/Ringo Starr - 1970 - 2019/2001 The Anthology...So Far (Koch KOC-CD-8312)/Disc 1`

### The Atlantic Years (1992, 2CD, 50 207-2)  (4)
    - `The Atlantic Years CD1 (1992, 2CD, 50 207-2)`
        - artist: Emerson, Lake & Palmer  ·  year: 1992
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1992. Emerson, Lake & Palmer - The Atlantic Years (1992, 2CD, Popron, Czech Republic, 50 207-2)/CD1`
    - `The Atlantic Years CD1 (7567-82403-2)`
        - artist: Emerson, Lake & Palmer  ·  year: 1992
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1992. Emerson, Lake & Palmer - The Atlantic Years (1992, 2CD, Atlantic, Germany, 7567-82403-2)/CD1`
    - `The Atlantic Years CD2 (1992, 2CD, 50 207-2)`
        - artist: Emerson, Lake & Palmer  ·  year: 1992
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1992. Emerson, Lake & Palmer - The Atlantic Years (1992, 2CD, Popron, Czech Republic, 50 207-2)/CD2`
    - `The Atlantic Years CD2 (7567-82403-2)`
        - artist: Emerson, Lake & Palmer  ·  year: 1992
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1992. Emerson, Lake & Palmer - The Atlantic Years (1992, 2CD, Atlantic, Germany, 7567-82403-2)/CD2`

### The Basement Tapes  (5)
    - `The Basement Tapes (CD 1)`
        - artist: Bob Dylan & The Band  ·  year: 1975
        - folder: `D:/music/_cd_rip/Dylan/Studio/1975 - The Basement Tapes/1975. Bob Dylan - The Basement Tapes (Columbia 466137 2, Austria)/Disc1`
    - `The Basement Tapes (CD 2)`
        - artist: Bob Dylan & The Band  ·  year: 1975
        - folder: `D:/music/_cd_rip/Dylan/Studio/1975 - The Basement Tapes/1975. Bob Dylan - The Basement Tapes (Columbia 466137 2, Austria)/Disc2`
    - `The Basement Tapes (Disc 2)`
        - artist: Bob Dylan and the Band  ·  year: 1977
        - folder: `D:/music/_cd_rip/Dylan/Studio/1975 - The Basement Tapes/1975. Bob Dylan - The Basement Tapes (Columbia C2K 33682, USA)/Disc2`
    - `The Basement Tapes [disc 1]`
        - artist: Bob Dylan & The Band  ·  year: 1975
        - folder: `D:/music/_cd_rip/Dylan/Studio/1975 - The Basement Tapes/1975. Bob Dylan - The Basement Tapes (Columbia C2K 33682, USA)/Disc1`
    - `The Basement Tapes [disc 2]`
        - artist: Bob Dylan & The Band  ·  year: 1975
        - folder: `D:/music/_cd_rip/Dylan/Studio/1975 - The Basement Tapes/1975. Bob Dylan - The Basement Tapes (Columbia 88691924312.19&20, EU)/Disc2`

### The Beatles (MFSL SuperVinyl 24/96)  (9)
    - `The Beatles   (MFSL SuperVinyl 24/96)`
        - artist: The Beatles  ·  year: 1968
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/10. 1968 - White Album {24bit.96kHz Vinyl Rip MFSL 2-072}/LP1`
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/10. 1968 - White Album {24bit.96kHz Vinyl Rip MFSL 2-072}/LP2`
    - `The Beatles (Disc 1)`
        - artist: The Beatles, Beatles  ·  year: 1998, 1968, 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Apple 7243 4 96895 2 7)/Disc 1`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Toshiba-EMI CP25-5329-30)/Disc 1`
    - `The Beatles (Disc 1) (CDS 7 46443 8)`
        - artist: The Beatles  ·  year: 1987
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Parl. CDS 7 46443 8)/Disc 1`
    - `The Beatles (Disc 15)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/15 The Beatles`
    - `The Beatles (Disc 2)`
        - artist: The Beatles, Beatles, The  ·  year: 1998, 1968, 2009
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Apple 7243 4 96895 2 7)/Disc 2`
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Toshiba-EMI CP25-5329-30)/Disc 2`
    - `The Beatles (Disc 2) (CDS 7 46443 8)`
        - artist: The Beatles  ·  year: 1987
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Parl. CDS 7 46443 8)/Disc 2`
    - `The Beatles (Disc1)`
        - artist: The Beatles  ·  year: 1998
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Toshiba-EMI TOCP-51119-20)/Disc 1`
    - `The Beatles (Disc2)`
        - artist: The Beatles  ·  year: 1998
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/1 Studio/1968 - The Beatles/The Beatles (Toshiba-EMI TOCP-51119-20)/Disc 2`
    - `The Beatles (No. 1) (Disc 3)`
        - artist: The Beatles  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - Compact Disc EP. Collection/03 The Beatles (No. 1)`

### The Best Of (2012, Japan, Universal, UICY-25269~70)  (2)
    - `The Best Of (2012, Japan, Universal, UICY-25269~70, CD1)`
        - artist: Motorhead  ·  year: 2000
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2000-The Best Of (2012, Japan, Universal, UICY-25269~70, 2CD)/CD1`
    - `The Best Of (2012, Japan, Universal, UICY-25269~70, CD2)`
        - artist: Motorhead  ·  year: 2000
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2000-The Best Of (2012, Japan, Universal, UICY-25269~70, 2CD)/CD2`

### The Best Of Hammond & Trumpet  (2)
    - `The Best Of Hammond & Trumpet CD1`
        - artist: James Last & His Orchestra  ·  year: 2002
        - folder: `D:/music/JAMES LAST COLLECTION/2002 - The Best Of Hammond & Trumpet (Universal Music, China) 2CD`
    - `The Best Of Hammond & Trumpet CD2`
        - artist: James Last & His Orchestra  ·  year: 2002
        - folder: `D:/music/JAMES LAST COLLECTION/2002 - The Best Of Hammond & Trumpet (Universal Music, China) 2CD`

### The Best Of The Best [Snapper, SMACD926, Germany]  (2)
    - `The Best Of The Best CD1 [Snapper, SMACD926, Germany]`
        - artist: W.A.S.P.  ·  year: 2000
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/2007 - The Best Of The Best 2CD [Snapper, SMACD926, Germany]`
    - `The Best Of The Best CD2 [Snapper, SMACD926, Germany]`
        - artist: W.A.S.P.  ·  year: 2000
        - folder: `D:/music/_cd_rip/W.A.S.P/Live & Compilation/2007 - The Best Of The Best 2CD [Snapper, SMACD926, Germany]`

### The Best Of The Cutting Edge  (2)
    - `The Best Of The Cutting Edge CD1`
        - artist: Bob Dylan  ·  year: 2015
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2015. Bob Dylan - The Bootleg Series Vol.12 The Best Of The Cutting Edge 1965-1966 (Columbia-Legacy 88875124422, EU)/Disc 1`
    - `The Best of The Cutting Edge CD2`
        - artist: Bob Dylan  ·  year: 2015
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/2015. Bob Dylan - The Bootleg Series Vol.12 The Best Of The Cutting Edge 1965-1966 (Columbia-Legacy 88875124422, EU)/Disc 2`

### The Book Of Souls: Live Chapter (WPCR-17952)  (2)
    - `The Book Of Souls: Live Chapter CD1 (WPCR-17952)`
        - artist: Iron Maiden  ·  year: 2017
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/2017 The Book Of Souls Live Chapter/2017 The Book Of Souls Live Chapter [2017 Japan WPCR-17952-3 Parlophone]/CD 1`
    - `The Book Of Souls: Live Chapter CD2 (WPCR-17953)`
        - artist: Iron Maiden  ·  year: 2017
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Live Albums/2017 The Book Of Souls Live Chapter/2017 The Book Of Souls Live Chapter [2017 Japan WPCR-17952-3 Parlophone]/CD 2`

### The Bootleg Series Volumes 1-3 (Rare & Unreleased) 1961-1991  (3)
    - `The Bootleg Series Volumes 1-3 (Rare & Unreleased) 1961-1991 [Disc 1]`
        - artist: Dylan, Bob  ·  year: 1991
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/1991. Bob Dyaln - The Bootleg Series Volumes 1-3 1961-1991 (Columbia 88697732882, EU)/Disc1`
    - `The Bootleg Series Volumes 1-3 (Rare & Unreleased) 1961-1991 [Disc 2]`
        - artist: Dylan, Bob  ·  year: 1991
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/1991. Bob Dyaln - The Bootleg Series Volumes 1-3 1961-1991 (Columbia 88697732882, EU)/Disc2`
    - `The Bootleg Series Volumes 1-3 (Rare & Unreleased) 1961-1991 [Disc 3]`
        - artist: Dylan, Bob  ·  year: 1991
        - folder: `D:/music/_cd_rip/Dylan/Bootleg Series/1991. Bob Dyaln - The Bootleg Series Volumes 1-3 1961-1991 (Columbia 88697732882, EU)/Disc3`

### The Casablanca Singles 1974-1982  (29)
    - `The Casablanca Singles 1974-1982 CD01`
        - artist: Kiss  ·  year: 1974
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD01 [B0017193-02 JK01]`
    - `The Casablanca Singles 1974-1982 CD02`
        - artist: Kiss  ·  year: 1974
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD02 [B0017193-02 JK02]`
    - `The Casablanca Singles 1974-1982 CD03`
        - artist: Kiss  ·  year: 1974
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD03 [B0017193-02 JK03]`
    - `The Casablanca Singles 1974-1982 CD04`
        - artist: Kiss  ·  year: 1974
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD04 [B0017193-02 JK04]`
    - `The Casablanca Singles 1974-1982 CD05`
        - artist: Kiss  ·  year: 1975
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD05 [B0017193-02 JK05]`
    - `The Casablanca Singles 1974-1982 CD06`
        - artist: Kiss  ·  year: 1975
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD06 [B0017193-02 JK06]`
    - `The Casablanca Singles 1974-1982 CD07`
        - artist: Kiss  ·  year: 1975
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD07 [B0017193-02 JK07]`
    - `The Casablanca Singles 1974-1982 CD08`
        - artist: Kiss  ·  year: 1976
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD08 [B0017193-02 JK08]`
    - `The Casablanca Singles 1974-1982 CD09`
        - artist: Kiss  ·  year: 1976
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD09 [B0017193-02 JK09]`
    - `The Casablanca Singles 1974-1982 CD10`
        - artist: Kiss  ·  year: 1976
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD10 [B0017193-02 JK10]`
    - `The Casablanca Singles 1974-1982 CD11`
        - artist: Kiss  ·  year: 1976
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD11 [B0017193-02 JK11]`
    - `The Casablanca Singles 1974-1982 CD12`
        - artist: Kiss  ·  year: 1976
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD12 [B0017193-02 JK12]`
    - `The Casablanca Singles 1974-1982 CD13`
        - artist: Kiss  ·  year: 1976
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD13 [B0017193-02 JK13]`
    - `The Casablanca Singles 1974-1982 CD14`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD14 [B0017193-02 JK14]`
    - `The Casablanca Singles 1974-1982 CD15`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD15 [B0017193-02 JK15]`
    - `The Casablanca Singles 1974-1982 CD16`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD16 [B0017193-02 JK16]`
    - `The Casablanca Singles 1974-1982 CD17`
        - artist: Kiss  ·  year: 1977
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD17 [B0017193-02 JK17]`
    - `The Casablanca Singles 1974-1982 CD18`
        - artist: Kiss  ·  year: 1978
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD18 [B0017193-02 JK18]`
    - `The Casablanca Singles 1974-1982 CD19`
        - artist: Kiss  ·  year: 1978
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD19 [B0017193-02 JK19]`
    - `The Casablanca Singles 1974-1982 CD20`
        - artist: Kiss  ·  year: 1978
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD20 [B0017193-02 JK20]`
    - `The Casablanca Singles 1974-1982 CD21`
        - artist: Kiss  ·  year: 1978
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD21 [B0017193-02 JK21]`
    - `The Casablanca Singles 1974-1982 CD22`
        - artist: Kiss  ·  year: 1978
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD22 [B0017193-02 JK22]`
    - `The Casablanca Singles 1974-1982 CD23`
        - artist: Kiss  ·  year: 1978
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD23 [B0017193-02 JK23]`
    - `The Casablanca Singles 1974-1982 CD24`
        - artist: Kiss  ·  year: 1979
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD24 [B0017193-02 JK24]`
    - `The Casablanca Singles 1974-1982 CD25`
        - artist: Kiss  ·  year: 1979
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD25 [B0017193-02 JK25]`
    - `The Casablanca Singles 1974-1982 CD26`
        - artist: Kiss  ·  year: 1980
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD26 [B0017193-02 JK26]`
    - `The Casablanca Singles 1974-1982 CD27`
        - artist: Kiss  ·  year: 1980
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD27 [B0017193-02 JK27]`
    - `The Casablanca Singles 1974-1982 CD28`
        - artist: Kiss  ·  year: 1981
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD28 [B0017193-02 JK28]`
    - `The Casablanca Singles 1974-1982 CD29`
        - artist: Kiss  ·  year: 1982
        - folder: `D:/music/_cd_rip/Kiss  Дискография  (1974 - 2015)/Box Set/2012 The Casablanca Singles 1974-1982 [B0017193-02 USA]/CD29 [B0017193-02 JK29]`

### The Chase Is Better Than The Catch  (2)
    - `The Chase Is Better Than The Catch (CD1)`
        - artist: Motorhead  ·  year: 2000
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2000-The Chase Is Better Than The Catch (2001, UK, Castle, CMDDD175, 2CD)/CD1`
    - `The Chase Is Better Than The Catch (CD2)`
        - artist: Motorhead  ·  year: 2000
        - folder: `D:/music/_cd_rip/Motorhead/Compilations/2000-The Chase Is Better Than The Catch (2001, UK, Castle, CMDDD175, 2CD)/CD2`

### The Classical Collection  (2)
    - `The Classical Collection (CD 1)`
        - artist: James Last  ·  year: 2003
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2003 - The Classical Collection [Universal Classics 9810457] 2CD/CD1`
    - `The Classical Collection (CD 2)`
        - artist: James Last  ·  year: 2003
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/2003 - The Classical Collection [Universal Classics 9810457] 2CD/CD2`

### The Collectable King Crimson Volume 3: Live At The Shepherds Bush Empire, London, 1996 [DGM5006]  (2)
    - `The Collectable King Crimson Volume 3: Live At The Shepherds Bush Empire, London, 1996 [DGM5006] CD1`
        - artist: King Crimson  ·  year: 1996
        - folder: `D:/music/King Crimson/5.Box, compilations/2008. The Collectable King Crimson Volume 3/2008, US, Discipline Global Mobile, DGM5006/CD 1`
    - `The Collectable King Crimson Volume 3: Live At The Shepherds Bush Empire, London, 1996 [DGM5006] CD2`
        - artist: King Crimson  ·  year: 2008
        - folder: `D:/music/King Crimson/5.Box, compilations/2008. The Collectable King Crimson Volume 3/2008, US, Discipline Global Mobile, DGM5006/CD 2`

### The Collectable King Crimson Volume 5: Live in Japan 1995 - The Official Edition [DGM5010]  (2)
    - `The Collectable King Crimson Volume 5: Live in Japan 1995 - The Official Edition [DGM5010] CD1`
        - artist: King Crimson  ·  year: 2010
        - folder: `D:/music/King Crimson/5.Box, compilations/2010. The Collectable King Crimson Volume 5/2010, US, Discipline Global Mobile, DGM5010/CD 1`
    - `The Collectable King Crimson Volume 5: Live in Japan 1995 - The Official Edition [DGM5010] CD2`
        - artist: King Crimson  ·  year: 2010
        - folder: `D:/music/King Crimson/5.Box, compilations/2010. The Collectable King Crimson Volume 5/2010, US, Discipline Global Mobile, DGM5010/CD 2`

### The Complete BBC Sessions  (3)
    - `The Complete BBC Sessions CD1`
        - artist: Led Zeppelin  ·  year: 2016
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/2016. Led Zeppelin - The Complete BBC Sessions (Atlantic 8122794389, EU)/Disc 1`
    - `The Complete BBC Sessions CD2`
        - artist: Led Zeppelin  ·  year: 2016
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/2016. Led Zeppelin - The Complete BBC Sessions (Atlantic 8122794389, EU)/Disc 2`
    - `The Complete BBC Sessions CD3`
        - artist: Led Zeppelin  ·  year: 2016
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1997. BBC Sessions/2016. Led Zeppelin - The Complete BBC Sessions (Atlantic 8122794389, EU)/Disc 3`

### The Concert For Bangla Desh  (2)
    - `The Concert For Bangla Desh (CD 1)`
        - artist: George Harrison, George Harrison & Ravi Shankar, Ravi Shankar  ·  year: 1971
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/02 Live Albums/1971 Concert For Bangla Desh (Epic 752.093-4-2-468835)/Disc 1`
    - `The Concert For Bangla Desh (CD2)`
        - artist: Bob Dylan, George Harrison, Leon Russell  ·  year: 1971
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/02 Live Albums/1971 Concert For Bangla Desh (Epic 752.093-4-2-468835)/Disc 2`

### The Concert for Bangladesh  (2)
    - `The Concert for Bangladesh (Disc 1)`
        - artist: George Harrison and friends  ·  year: 1971
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/02 Live Albums/1971 Concert For Bagla Desh  (Sony 82876729862)/Disc 1`
    - `The Concert for Bangladesh (Disc 2)`
        - artist: George Harrison and friends  ·  year: 1971
        - folder: `D:/music/_cd_rip/George Harrison - Discography - 1968 - 2014/02 Live Albums/1971 Concert For Bagla Desh  (Sony 82876729862)/Disc 2`

### The Crimson Idol [Recall 2CD, SMDCD145, UK]  (4)
    - `The Crimson Idol CD1 [Recall 2CD, SMDCD145, UK]`
        - artist: W.A.S.P.  ·  year: 1992
        - folder: `D:/music/_cd_rip/W.A.S.P/1992 - The Crimson Idol/1998 - The Crimson Idol 2CD [Recall 2CD, SMDCD145, UK]`
    - `The Crimson Idol CD1 [Victor, VICP 60334~5, Japan]`
        - artist: W.A.S.P.  ·  year: 1992
        - folder: `D:/music/_cd_rip/W.A.S.P/1992 - The Crimson Idol/1998 - The Crimson Idol 2CD [Victor, VICP 60334~5, Japan]`
    - `The Crimson Idol CD2 [Recall 2CD, SMDCD145, UK]`
        - artist: W.A.S.P.  ·  year: 1992
        - folder: `D:/music/_cd_rip/W.A.S.P/1992 - The Crimson Idol/1998 - The Crimson Idol 2CD [Recall 2CD, SMDCD145, UK]`
    - `The Crimson Idol CD2 [Victor, VICP 60334~5, Japan]`
        - artist: W.A.S.P.  ·  year: 1992
        - folder: `D:/music/_cd_rip/W.A.S.P/1992 - The Crimson Idol/1998 - The Crimson Idol 2CD [Victor, VICP 60334~5, Japan]`

### The Dark Side Of The Moon  (2)
    - `The Dark Side Of The Moon CD1`
        - artist: Pink Floyd  ·  year: 1973
        - folder: `D:/music/Pink Floyd/Studio/1973 The Dark Side Of The Moon/[50999 029453 2 3] [EU, 2011 Experience]/CD1`
    - `The Dark Side Of The Moon CD2`
        - artist: Pink Floyd  ·  year: 1974
        - folder: `D:/music/Pink Floyd/Studio/1973 The Dark Side Of The Moon/[50999 029453 2 3] [EU, 2011 Experience]/CD2`

### The Deep Purple Collection (2011, 3CD, 0600753335505)  (3)
    - `The Deep Purple Collection CD1 (2011, 3CD, 0600753335505)`
        - artist: Deep Purple  ·  year: 2011
        - folder: `D:/music/_cd_rip/Deep Purple/Compilation Albums/2011. The Deep Purple Collection (2011, 3CD, Universal, EU, Germany, 0600753335505)/CD1`
    - `The Deep Purple Collection CD2 (2011, 3CD, 0600753335505)`
        - artist: Deep Purple  ·  year: 2011
        - folder: `D:/music/_cd_rip/Deep Purple/Compilation Albums/2011. The Deep Purple Collection (2011, 3CD, Universal, EU, Germany, 0600753335505)/CD2`
    - `The Deep Purple Collection CD3 (2011, 3CD, 0600753335505)`
        - artist: Deep Purple  ·  year: 2011
        - folder: `D:/music/_cd_rip/Deep Purple/Compilation Albums/2011. The Deep Purple Collection (2011, 3CD, Universal, EU, Germany, 0600753335505)/CD3`

### The Downward Spiral - Deluxe Edition  (2)
    - `The Downward Spiral - Deluxe Edition [ Disc 1 ]`
        - artist: Nine Inch Nails  ·  year: 1994
        - folder: `D:/music/Nine Inch Nails - The Downward Spiral (1994) [SACD] (2004 Deluxe Edition ISO)/Disc 1 of 2`
    - `The Downward Spiral - Deluxe Edition [ Disc 2 ]`
        - artist: Nine Inch Nails  ·  year: 1994
        - folder: `D:/music/Nine Inch Nails - The Downward Spiral (1994) [SACD] (2004 Deluxe Edition ISO)/Disc 2 of 2`

### The Early Years: 1965-1967 Cambridge St/ation PFREY1  (2)
    - `The Early Years: 1965-1967 Cambridge St/ation PFREY1 CD1`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/BoxSet/2016 The Early Years 1965-1972/01. 1965-1967 Cambridge St-ation (PFR PFREY1, EU)/CD01`
    - `The Early Years: 1965-1967 Cambridge St/ation PFREY1 CD2`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/BoxSet/2016 The Early Years 1965-1972/01. 1965-1967 Cambridge St-ation (PFR PFREY1, EU)/CD02`

### The Early Years: 1969 Dramatis/ation PFREY3  (2)
    - `The Early Years: 1969 Dramatis/ation PFREY3 CD1`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/BoxSet/2016 The Early Years 1965-1972/03. 1969 Dramatis-ation  (PFR PFREY3, EU)/CD01`
    - `The Early Years: 1969 Dramatis/ation PFREY3 CD2`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/BoxSet/2016 The Early Years 1965-1972/03. 1969 Dramatis-ation  (PFR PFREY3, EU)/CD02`

### The Early Years: 1970 Devi/ation PFREY4  (2)
    - `The Early Years: 1970 Devi/ation PFREY4 CD1`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/BoxSet/2016 The Early Years 1965-1972/04. 1970 Devi-ation  (PFR PFREY4, EU)/CD01`
    - `The Early Years: 1970 Devi/ation PFREY4 CD2`
        - artist: Pink Floyd  ·  year: 2016
        - folder: `D:/music/Pink Floyd/BoxSet/2016 The Early Years 1965-1972/04. 1970 Devi-ation  (PFR PFREY4, EU)/CD02`

### The Elements (2014 Tour Box) [KCTB14]  (6)
    - `The Elements (2014 Tour Box) [KCTB14] CD1`
        - artist: King Crimson  ·  year: 2014
        - folder: `D:/music/King Crimson/3.Live/2014. The Elements (2014 Tour Box)/2014, EU, Discipline Global Mobile, KCTB14/CD 1`
    - `The Elements (2014 Tour Box) [KCTB14] CD2`
        - artist: King Crimson, Jakszyk, Fripp, Collins, ProjeKct Four  ·  year: 2014
        - folder: `D:/music/King Crimson/3.Live/2014. The Elements (2014 Tour Box)/2014, EU, Discipline Global Mobile, KCTB14/CD 2`
    - `The Elements (2015 Tour Box) [KCTB15] CD1`
        - artist: King Crimson  ·  year: 2015
        - folder: `D:/music/King Crimson/3.Live/2015. The Elements (2015 Tour Box)/2015, EU, Discipline Global Mobile, KCTB15/CD 1`
    - `The Elements (2015 Tour Box) [KCTB15] CD2`
        - artist: King Crimson, ProjeKct Four, ProjeKct Three  ·  year: 2015
        - folder: `D:/music/King Crimson/3.Live/2015. The Elements (2015 Tour Box)/2015, EU, Discipline Global Mobile, KCTB15/CD 2`
    - `The Elements (2016 Tour Box) [KCTB16] CD1`
        - artist: King Crimson  ·  year: 2016
        - folder: `D:/music/King Crimson/3.Live/2016. The Elements (2016 Tour Box)/2016, EU, Discipline Global Mobile, KCTB16/CD 1`
    - `The Elements (2016 Tour Box) [KCTB16] CD2`
        - artist: King Crimson  ·  year: 2016
        - folder: `D:/music/King Crimson/3.Live/2016. The Elements (2016 Tour Box)/2016, EU, Discipline Global Mobile, KCTB16/CD 2`

### The Essential  (8)
    - `The Essential (CD1)`
        - artist: Leonard Cohen  ·  year: 2002
        - folder: `D:/music/_cd_rip/Leonard Cohen - Discography/27. Leonard Cohen - The Essential Leonard Cohen - 2002 (2CD)/Leonard Cohen - The Essential Leonard Cohen - 2002 {Columbia 497995 2}/CD 1`
        - folder: `D:/music/_cd_rip/Leonard Cohen - Discography/27. Leonard Cohen - The Essential Leonard Cohen - 2002 (2CD)/Leonard Cohen - The Essential Leonard Cohen - 2002 {Columbia 86884}/CD 1`
    - `The Essential (CD2)`
        - artist: Leonard Cohen  ·  year: 2002
        - folder: `D:/music/_cd_rip/Leonard Cohen - Discography/27. Leonard Cohen - The Essential Leonard Cohen - 2002 (2CD)/Leonard Cohen - The Essential Leonard Cohen - 2002 {Columbia 497995 2}/CD 2`
        - folder: `D:/music/_cd_rip/Leonard Cohen - Discography/27. Leonard Cohen - The Essential Leonard Cohen - 2002 (2CD)/Leonard Cohen - The Essential Leonard Cohen - 2002 {Columbia 86884}/CD 2`
    - `The Essential (Disc 1)`
        - artist: Barbra Streisand, The Alan Parsons Project  ·  year: 2002, 2007
        - folder: `D:/music/Barbra Streisand/2002 - The Essential/CD1`
        - folder: `D:/music/_cd_rip/The Alan Parsons Project/2007 - The Essential (2008 - Arista • BMG Japan - BVCM-35558•9)/CD1`
    - `The Essential (Disc 2)`
        - artist: Barbra Streisand, The Alan Parsons Project  ·  year: 2002, 2007
        - folder: `D:/music/Barbra Streisand/2002 - The Essential/CD2`
        - folder: `D:/music/_cd_rip/The Alan Parsons Project/2007 - The Essential (2008 - Arista • BMG Japan - BVCM-35558•9)/CD2`
    - `The Essential CD1`
        - artist: Iron Maiden  ·  year: 2005
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Compilation/2005 The Essential Iron Maiden/2005 The Essential Iron Maiden [2005 USA C2K 92832 Sanctuary]/CD 1`
    - `The Essential CD1 (2011, 2CD, 88697830452)`
        - artist: Emerson, Lake & Palmer  ·  year: 2011
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2011. Emerson, Lake & Palmer - The Essential (2011, 2CD, Sony, EU, France, 88697830452)/CD1`
    - `The Essential CD2`
        - artist: Iron Maiden  ·  year: 2005
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Compilation/2005 The Essential Iron Maiden/2005 The Essential Iron Maiden [2005 USA C2K 92832 Sanctuary]/CD 2`
    - `The Essential CD2 (2011, 2CD, 88697830452)`
        - artist: Emerson, Lake & Palmer  ·  year: 2011
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/2011. Emerson, Lake & Palmer - The Essential (2011, 2CD, Sony, EU, France, 88697830452)/CD2`

### The Essential 3.0 (2009, Sony, SICP 2533, Japan)  (3)
    - `The Essential 3.0 (2009, Sony, SICP 2533, Japan, CD 1)`
        - artist: Judas Priest  ·  year: 2010
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2008 - The Essential 3.0 [Sony, SICP 2533~5, Japan]`
    - `The Essential 3.0 (2009, Sony, SICP 2534, Japan, CD 2)`
        - artist: Judas Priest  ·  year: 2010
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2008 - The Essential 3.0 [Sony, SICP 2533~5, Japan]`
    - `The Essential 3.0 (2009, Sony, SICP 2535, Japan, CD 3)`
        - artist: Judas Priest  ·  year: 2010
        - folder: `D:/music/_cd_rip/Judas Priest/Boxsets & Compilations/2008 - The Essential 3.0 [Sony, SICP 2533~5, Japan]`

### The Essential James Last  (2)
    - `The Essential James Last -  CD 2`
        - artist: James Last  ·  year: 2006
        - folder: `D:/music/James Last, flac/2006 - The Essential (2 CD)/CD 2`
    - `The Essential James Last - CD 1`
        - artist: James Last  ·  year: 2006
        - folder: `D:/music/James Last, flac/2006 - The Essential (2 CD)/CD 1`

### The Fame Monster (Japanese Deluxe Edition)  (8)
    - `The Fame Monster (Japanese Deluxe Edition) - CD 1`
        - artist: Lady GaGa  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (Japanese Deluxe Edition)/CD 1`
    - `The Fame Monster (Japanese Deluxe Edition) - CD 2`
        - artist: Lady GaGa  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (Japanese Deluxe Edition)/CD 2`
    - `The Fame Monster (Russian Deluxe Edition) - CD 1`
        - artist: Lady GaGa  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (Russian Deluxe Edition)/CD 1`
    - `The Fame Monster (Russian Deluxe Edition) - CD 2`
        - artist: Lady Gaga  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (Russian Deluxe Edition)/CD 2`
    - `The Fame Monster (UK Deluxe Edition) - CD 1`
        - artist: Lady GaGa  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (UK Deluxe Edition)/CD 1`
    - `The Fame Monster (UK Deluxe Edition) - CD 2`
        - artist: Lady GaGa  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (UK Deluxe Edition)/CD 2`
    - `The Fame Monster (USA Super Deluxe) CD 1`
        - artist: Lady Gaga  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (USA Super Deluxe)/CD 1`
    - `The Fame Monster (USA Super Deluxe) CD 2`
        - artist: Lady Gaga  ·  year: 2009
        - folder: `D:/music/_cd_rip/Lady GaGa - Fame Monster (Albums and Singles)/Albums/Lady GaGa - The Fame Monster (USA Super Deluxe)/CD 2`

### The Final Chapter  (2)
    - `The Final Chapter (Disc 1)`
        - artist: Accept  ·  year: 1998
        - folder: `D:/music/_cd_rip/Accept - Discography/Live Albums/1998 The Final Chapter/1998 The Final Chapter [1998 CMC 06076 86232-2 USA]/CD1`
    - `The Final Chapter (Disc 2)`
        - artist: Accept  ·  year: 1998
        - folder: `D:/music/_cd_rip/Accept - Discography/Live Albums/1998 The Final Chapter/1998 The Final Chapter [1998 CMC 06076 86232-2 USA]/CD2`

### The Final Countdown The Best Of Europe  (2)
    - `The Final Countdown The Best Of Europe CD1`
        - artist: Europe  ·  year: 2009
        - folder: `D:/music/_cd_rip/EUROPE - Discorgaphy (lossless)/Compilations/2009-06-01 - The Final Countdown The Best Of Europe (2CD) [88697536572, 2009] {dmvitaly32}`
    - `The Final Countdown The Best Of Europe CD2`
        - artist: Europe  ·  year: 2009
        - folder: `D:/music/_cd_rip/EUROPE - Discorgaphy (lossless)/Compilations/2009-06-01 - The Final Countdown The Best Of Europe (2CD) [88697536572, 2009] {dmvitaly32}`

### The Gospel Road  (2)
    - `The Gospel Road CD1`
        - artist: Johnny Cash, Johnny Cash with The Carter Family and The Statler Brothers, Larry Gatlin, Kris Kristofferson and Rita Coolidge  ·  year: 1973
        - folder: `D:/music/Cash - Collection/2012. Johnny Cash - The Complete Columbia Album Collection (Columbia 88697910472, EU)/32. The Gospel Road (disc 1) (1973)`
    - `The Gospel Road CD2`
        - artist: Johnny Cash  ·  year: 1973
        - folder: `D:/music/Cash - Collection/2012. Johnny Cash - The Complete Columbia Album Collection (Columbia 88697910472, EU)/33. The Gospel Road (disc 2) (1973)`

### The Great Deceiver: Part One [DGM5004]  (2)
    - `The Great Deceiver: Part One [DGM5004] CD1`
        - artist: King Crimson  ·  year: 2007
        - folder: `D:/music/King Crimson/3.Live/2007. The Great Deceiver - Part One/2007, US, Discipline Global Mobile, DGM5004/CD 1`
    - `The Great Deceiver: Part One [DGM5004] CD2`
        - artist: King Crimson  ·  year: 2007
        - folder: `D:/music/King Crimson/3.Live/2007. The Great Deceiver - Part One/2007, US, Discipline Global Mobile, DGM5004/CD 2`

### The Great Deceiver: Part Two [DGM5005]  (2)
    - `The Great Deceiver: Part Two [DGM5005] CD1`
        - artist: King Crimson  ·  year: 2007
        - folder: `D:/music/King Crimson/3.Live/2007. The Great Deceiver - Part Two/2007, US, Discipline Global Mobile, DGM5005/CD 1`
    - `The Great Deceiver: Part Two [DGM5005] CD2`
        - artist: King Crimson  ·  year: 2007
        - folder: `D:/music/King Crimson/3.Live/2007. The Great Deceiver - Part Two/2007, US, Discipline Global Mobile, DGM5005/CD 2`

### The Hits  (2)
    - `The Hits Disc1`
        - artist: Prince  ·  year: 1993
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1993 - The Hits-The B-Sides (3 CD set) (compilation)/The_Hits_Disc1`
    - `The Hits Disc2`
        - artist: Prince  ·  year: 1993
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/1993 - The Hits-The B-Sides (3 CD set) (compilation)/The_Hits_Disc2`

### The Incident  (4)
    - `The Incident (Disc 1)`
        - artist: Porcupine Tree  ·  year: 2009
        - folder: `D:/music/Porcupine Tree Main discography/Japan Releases/2009 - The Incident 2CD (WHD Entertainment, Inc. IECP-10198 Japan)/CD1`
    - `The Incident (Disc 2)`
        - artist: Porcupine Tree  ·  year: 2009
        - folder: `D:/music/Porcupine Tree Main discography/Japan Releases/2009 - The Incident 2CD (WHD Entertainment, Inc. IECP-10198 Japan)/CD2`
    - `The Incident CD1`
        - artist: Porcupine Tree  ·  year: 2009
        - folder: `D:/music/Porcupine Tree Main discography/Original Albums/2009 - The Incident 2CD (Roadrunner Records RR 7857-2 UK & Europe)/CD1`
    - `The Incident CD2`
        - artist: Porcupine Tree  ·  year: 2009
        - folder: `D:/music/Porcupine Tree Main discography/Original Albums/2009 - The Incident 2CD (Roadrunner Records RR 7857-2 UK & Europe)/CD2`

### The Lamb Lies Down On Broadway (CGSCD 1)  (4)
    - `The Lamb Lies Down On Broadway (CGSCD 1) CD1`
        - artist: Genesis  ·  year: 1974
        - folder: `D:/music/GENESIS Studio Discography (1969-1997)/Albums/1974 - The Lamb Lies Down On Broadway (CGSCD 1) Nimbus/CD 1`
    - `The Lamb Lies Down On Broadway (CGSCD 1) CD2`
        - artist: Genesis  ·  year: 1974
        - folder: `D:/music/GENESIS Studio Discography (1969-1997)/Albums/1974 - The Lamb Lies Down On Broadway (CGSCD 1) Nimbus/CD 2`
    - `The Lamb Lies Down On Broadway - CD 1`
        - artist: Genesis  ·  year: 1974
        - folder: `D:/music/GENESIS Studio Discography (1969-1997)/Albums/1974 - The Lamb Lies Down On Broadway (401-2)/CD 1`
    - `The Lamb Lies Down On Broadway - CD 2`
        - artist: Genesis  ·  year: 1974
        - folder: `D:/music/GENESIS Studio Discography (1969-1997)/Albums/1974 - The Lamb Lies Down On Broadway (401-2)/CD 2`

### The Last Command [Madfish, SMACD967, Poland]  (2)
    - `The Last Command CD1 [Madfish, SMACD967, Poland]`
        - artist: W.A.S.P.  ·  year: 2010
        - folder: `D:/music/_cd_rip/W.A.S.P/1985 - The Last Command/2010 - The Last Command 2CD [Madfish, SMACD967, Poland]`
    - `The Last Command CD2 [Madfish, SMACD967, Poland]`
        - artist: W.A.S.P.  ·  year: 2010
        - folder: `D:/music/_cd_rip/W.A.S.P/1985 - The Last Command/2010 - The Last Command 2CD [Madfish, SMACD967, Poland]`

### The Last In Line (2012, Deluxe Expanded Edition)  (3)
    - `The Last In Line (2012, Deluxe Expanded Edition, Disc 1)`
        - artist: Dio  ·  year: 1984
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1984 The Last In Line/1984 The Last In Line [2012 Universal 2776098 Germany]/Disc 1 - The Last In Line`
    - `The Last In Line (2012, Deluxe Expanded Edition, Disc 2)`
        - artist: Dio  ·  year: 1984
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Studio Albums/1984 The Last In Line/1984 The Last In Line [2012 Universal 2776098 Germany]/Dsic 2 - Bonus Tracks`
    - `The Last in Line (The Singles Collection, Disc 3)`
        - artist: Dio  ·  year: 2012
        - folder: `D:/music/_cd_rip/DIO 1983-2010/Box Set/2012 The Singles Box Set [Universal 006025 2799275 4 EU]/Disc 3 - The Last in Line (CDS, 00602537091379)`

### The NazBox (SALVOSBX409)  (4)
    - `The NazBox (SALVOSBX409) CD1`
        - artist: Nazareth  ·  year: 2011
        - folder: `D:/music/_cd_rip/Nazareth discography/Box Sets/2011. Nazareth - The NazBox/2011. Nazareth - The NazBox (Salvo, EU, UK, SALVOBX409)/CD1`
    - `The NazBox (SALVOSBX409) CD2`
        - artist: Nazareth  ·  year: 2011
        - folder: `D:/music/_cd_rip/Nazareth discography/Box Sets/2011. Nazareth - The NazBox/2011. Nazareth - The NazBox (Salvo, EU, UK, SALVOBX409)/CD2`
    - `The NazBox (SALVOSBX409) CD3`
        - artist: Nazareth  ·  year: 2011
        - folder: `D:/music/_cd_rip/Nazareth discography/Box Sets/2011. Nazareth - The NazBox/2011. Nazareth - The NazBox (Salvo, EU, UK, SALVOBX409)/CD3`
    - `The NazBox (SALVOSBX409) CD4`
        - artist: Nazareth  ·  year: 2011
        - folder: `D:/music/_cd_rip/Nazareth discography/Box Sets/2011. Nazareth - The NazBox/2011. Nazareth - The NazBox (Salvo, EU, UK, SALVOBX409)/CD4`

### The Night Watch [2006, DGM9707]  (2)
    - `The Night Watch [2006, DGM9707] CD1`
        - artist: King Crimson  ·  year: 1997
        - folder: `D:/music/King Crimson/3.Live/1997. The Night Watch/2006, US, Discipline Global Mobile, DGM 9707/CD 1`
    - `The Night Watch [2006, DGM9707] CD2`
        - artist: King Crimson  ·  year: 1997
        - folder: `D:/music/King Crimson/3.Live/1997. The Night Watch/2006, US, Discipline Global Mobile, DGM 9707/CD 2`

### The Number Of The Beast (7243 8 35870 2 5)  (4)
    - `The Number Of The Beast (7243 8 35870 2 5, CD1)`
        - artist: Iron Maiden  ·  year: 1982
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1982 The Number Of The Beast/1982 The Number Of The Beast [1995 Holland 7243 8 35870 2 5 EMI]/CD 1`
    - `The Number Of The Beast (7243 8 35870 2 5, CD2)`
        - artist: Iron Maiden  ·  year: 1982
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1982 The Number Of The Beast/1982 The Number Of The Beast [1995 Holland 7243 8 35870 2 5 EMI]/CD 2`
    - `The Number Of The Beast [Castle 104-2] CD 1`
        - artist: Iron Maiden  ·  year: 1982
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1982 The Number Of The Beast/1982 The Number Of The Beast [1995 USA CASTLE 104-2. Castle Records]/CD 1`
    - `The Number Of The Beast [Castle 104-2] CD 2`
        - artist: Iron Maiden  ·  year: 1982
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1982 The Number Of The Beast/1982 The Number Of The Beast [1995 USA CASTLE 104-2. Castle Records]/CD 2`

### The Original Maxi-Singles Collection  (2)
    - `The Original Maxi-Singles Collection CD1`
        - artist: Bad Boys Blue  ·  year: 2014
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2014.The Original Maxi-Singles Collection (2 CD)/The Original Maxi-Singles Collection CD1`
    - `The Original Maxi-Singles Collection CD2`
        - artist: Bad Boys Blue  ·  year: 2014
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2014.The Original Maxi-Singles Collection (2 CD)/The Original Maxi-Singles Collection CD2`

### The Original Maxi-Singles Collection Volume 2  (2)
    - `The Original Maxi-Singles Collection Volume 2 CD1`
        - artist: Bad Boys Blue  ·  year: 2015
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2015.The Original Maxi-Singles Collection Volume 2  (2 CD)/The Original Maxi-Singles Collection Volume 2 CD1`
    - `The Original Maxi-Singles Collection Volume 2 CD2`
        - artist: Bad Boys Blue  ·  year: 2015
        - folder: `D:/music/Bad Boys Blue - Discography (lossless)/Albums & Compilations/2015.The Original Maxi-Singles Collection Volume 2  (2 CD)/The Original Maxi-Singles Collection Volume 2 CD2`

### The Ozzy Osbourne Years  (3)
    - `The Ozzy Osbourne Years (Disc 1)`
        - artist: Black Sabbath  ·  year: 1991
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Compilations/1991 The Ozzy Osbourne Years/1991 The Ozzy Osbourne Years [UK ESBCD 142 1 2 3 Castle]/CD 1`
    - `The Ozzy Osbourne Years (Disc 2)`
        - artist: Black Sabbath  ·  year: 1991
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Compilations/1991 The Ozzy Osbourne Years/1991 The Ozzy Osbourne Years [UK ESBCD 142 1 2 3 Castle]/CD 2`
    - `The Ozzy Osbourne Years (Disc 3)`
        - artist: Black Sabbath  ·  year: 1991
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Compilations/1991 The Ozzy Osbourne Years/1991 The Ozzy Osbourne Years [UK ESBCD 142 1 2 3 Castle]/CD 3`

### The Piper At The Gates Of Dawn [40th Anniversary Edition]  (3)
    - `The Piper At The Gates Of Dawn [40th Anniversary Edition] (Disc 1)`
        - artist: Pink Floyd  ·  year: 1967
        - folder: `D:/music/Pink Floyd/Studio/1967 The Piper at the Gates of Dawn/[50999 503919 2 9, Dlx] [EU, Rem 2007]/CD1 Mono`
    - `The Piper At The Gates Of Dawn [40th Anniversary Edition] (Disc 2)`
        - artist: Pink Floyd  ·  year: 1967
        - folder: `D:/music/Pink Floyd/Studio/1967 The Piper at the Gates of Dawn/[50999 503919 2 9, Dlx] [EU, Rem 2007]/CD2 Stereo`
    - `The Piper At The Gates Of Dawn [40th Anniversary Edition] (Disc 3)`
        - artist: Pink Floyd  ·  year: 2007
        - folder: `D:/music/Pink Floyd/Studio/1967 The Piper at the Gates of Dawn/[50999 503919 2 9, Dlx] [EU, Rem 2007]/CD3 Bonus Disc`

### The Platinum Collection  (2)
    - `The Platinum Collection CD 2`
        - artist: Scorpions  ·  year: 2005
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Compilations/2005 The Platinum Collection/2005 The Platinum Collection [2005 Germany 00946 340 407 23 EMI]/CD 2`
    - `The Platinum Collection Cd1`
        - artist: Scorpions  ·  year: 2005
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Compilations/2005 The Platinum Collection/2005 The Platinum Collection [2005 Germany 00946 340 407 23 EMI]/CD 1`

### The Police  (2)
    - `The Police (Disc 2)`
        - artist: Police  ·  year: 2007
        - folder: `D:/music/The Police/CD 2`
    - `The Police CD1`
        - artist: The Police  ·  year: 2007
        - folder: `D:/music/The Police/CD 1`

### The Police - 30 Tracks  (2)
    - `The Police - 30 Tracks [Disc One]`
        - artist: The Police  ·  year: 2007
        - folder: `D:/music/The Police - The Police (2007)/CD1`
    - `The Police - 30 Tracks [Disc Two]`
        - artist: The Police  ·  year: 2007
        - folder: `D:/music/The Police - The Police (2007)/CD2`

### The Real Royal Albert Hall 1966 Concert  (2)
    - `The Real Royal Albert Hall 1966 Concert (Disc 1)`
        - artist: Bob Dylan  ·  year: 2016
        - folder: `D:/music/_cd_rip/Dylan/Live/2016 - Royal Albert Hall 1966/2016. Bob Dylan - The Real Royal Albert Hall 1966 (Columbia-Legacy 88985374342, EU)/Disc 1`
    - `The Real Royal Albert Hall 1966 Concert (Disc 2)`
        - artist: Bob Dylan  ·  year: 2016
        - folder: `D:/music/_cd_rip/Dylan/Live/2016 - Royal Albert Hall 1966/2016. Bob Dylan - The Real Royal Albert Hall 1966 (Columbia-Legacy 88985374342, EU)/Disc 2`

### The Return Of The Manticore  (4)
    - `The Return Of The Manticore CD1`
        - artist: Emerson, Lake & Palmer  ·  year: 1993
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1993. Emerson, Lake & Palmer - The Return Of The Manticore (1993, 4CD, Victory Music, USA, 383 484 004-2)/CD1`
    - `The Return Of The Manticore CD2`
        - artist: Emerson, Lake & Palmer  ·  year: 1993
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1993. Emerson, Lake & Palmer - The Return Of The Manticore (1993, 4CD, Victory Music, USA, 383 484 004-2)/CD2`
    - `The Return Of The Manticore CD3`
        - artist: Emerson, Lake & Palmer  ·  year: 1993
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1993. Emerson, Lake & Palmer - The Return Of The Manticore (1993, 4CD, Victory Music, USA, 383 484 004-2)/CD3`
    - `The Return Of The Manticore CD4`
        - artist: Emerson, Lake & Palmer  ·  year: 1993
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Compilation Albums/1993. Emerson, Lake & Palmer - The Return Of The Manticore (1993, 4CD, Victory Music, USA, 383 484 004-2)/CD4`

### The Rolling Stones Rock and Roll Circus  (2)
    - `The Rolling Stones Rock and Roll Circus (CD1)`
        - artist: Various Artists, Mick Jagger, [unknown]  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Rock And Roll Circus [7185542]/CD1`
    - `The Rolling Stones Rock and Roll Circus (CD2)`
        - artist: Taj Mahal, The Dirty Mac, Julius Katchen  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Rock And Roll Circus [7185542]/CD2`

### The Show That Never Ends (2008, 2CD, SMD CD 370)  (2)
    - `The Show That Never Ends CD1 (2008, 2CD, SMD CD 370)`
        - artist: Emerson, Lake & Palmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2001. Emerson, Lake & Palmer - The Show That Never Ends (2008, 2CD, Snapper Music, EU, UK, SMD CD 370)/CD1`
    - `The Show That Never Ends CD2 (2008, 2CD, SMD CD 370)`
        - artist: Emerson, Lake & Palmer  ·  year: 2001
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/2001. Emerson, Lake & Palmer - The Show That Never Ends (2008, 2CD, Snapper Music, EU, UK, SMD CD 370)/CD2`

### The Singles (SALVOMDCD30)  (2)
    - `The Singles (SALVOMDCD30) CD1`
        - artist: Nazareth  ·  year: 2012
        - folder: `D:/music/_cd_rip/Nazareth discography/Singles/2012. Nazareth - The Singles/2012. Nazareth - The Singles (Salvo, EU, UK, SALVOMDCD30)/CD1`
    - `The Singles (SALVOMDCD30) CD2`
        - artist: Nazareth  ·  year: 2012
        - folder: `D:/music/_cd_rip/Nazareth discography/Singles/2012. Nazareth - The Singles/2012. Nazareth - The Singles (Salvo, EU, UK, SALVOMDCD30)/CD2`

### The Singles 1970-1978  (6)
    - `The Singles 1970-1978 (CD1)`
        - artist: Black Sabbath  ·  year: 1970
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2000 The Singles (1970-1978) [UK CMKBX002 Sanctuary]/CD1`
    - `The Singles 1970-1978 (CD2)`
        - artist: Black Sabbath  ·  year: 2000
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2000 The Singles (1970-1978) [UK CMKBX002 Sanctuary]/CD2`
    - `The Singles 1970-1978 (CD3)`
        - artist: Black Sabbath  ·  year: 2000
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2000 The Singles (1970-1978) [UK CMKBX002 Sanctuary]/CD3`
    - `The Singles 1970-1978 (CD4)`
        - artist: Black Sabbath  ·  year: 2000
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2000 The Singles (1970-1978) [UK CMKBX002 Sanctuary]/CD4`
    - `The Singles 1970-1978 (CD5)`
        - artist: Black Sabbath  ·  year: 2000
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2000 The Singles (1970-1978) [UK CMKBX002 Sanctuary]/CD5`
    - `The Singles 1970-1978 (CD6)`
        - artist: Black Sabbath  ·  year: 2000
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Box Set/2000 The Singles (1970-1978) [UK CMKBX002 Sanctuary]/CD6`

### The Singles 86 - 98  (2)
    - `The Singles 86 - 98 (Disc 1)`
        - artist: Depeche Mode  ·  year: 1998
        - folder: `D:/music/Depeche Mode/15. Depeche Mode - The Singles 86 - 98 (1998)/Depeche Mode - The Singles 86 - 98 (1998) (Mute Reprise 9 47110-2) (USA 1998)/Disc 1`
    - `The Singles 86 - 98 (Disc 2)`
        - artist: Depeche Mode  ·  year: 1998
        - folder: `D:/music/Depeche Mode/15. Depeche Mode - The Singles 86 - 98 (1998)/Depeche Mode - The Singles 86 - 98 (1998) (Mute Reprise 9 47110-2) (USA 1998)/Disc 2`

### The Singles Box Set 1975-1986 - Since You Been Gone  (2)
    - `The Singles Box Set 1975-1986 CD8 - Since You Been Gone`
        - artist: Rainbow  ·  year: 1979
        - folder: `D:/music/_cd_rip/Rainbow Discography/Singles/2013. Rainbow - The Singles Box Set 1975-1986 (2013, 19CD Boxset, Polydor, EU, 0600753460535)/CD8`
    - `The Singles Box Set 1975-1986 CD9 - Since You Been Gone`
        - artist: Rainbow  ·  year: 1979
        - folder: `D:/music/_cd_rip/Rainbow Discography/Singles/2013. Rainbow - The Singles Box Set 1975-1986 (2013, 19CD Boxset, Polydor, EU, 0600753460535)/CD9`

### The Singles • The First Ten Years  (2)
    - `The Singles • The First Ten Years (Disc 1)`
        - artist: ABBA  ·  year: 1982
        - folder: `D:/music/ABBA®/1982 - The Singles • The First Ten Years (1984 - Clear Polar - POLCD 401-2 • 810 050-2 YH 2)/CD1`
    - `The Singles • The First Ten Years (Disc 2)`
        - artist: ABBA  ·  year: 1982
        - folder: `D:/music/ABBA®/1982 - The Singles • The First Ten Years (1984 - Clear Polar - POLCD 401-2 • 810 050-2 YH 2)/CD2`

### The Sky Moves Sideways  (2)
    - `The Sky Moves Sideways CD1`
        - artist: Porcupine Tree  ·  year: 2003
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1995 - The Sky Moves Sideways 2CD (Remaster 2003) (Kscope KSCOPE124 UK)/CD1`
    - `The Sky Moves Sideways CD2`
        - artist: Porcupine Tree  ·  year: 2003
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1995 - The Sky Moves Sideways 2CD (Remaster 2003) (Kscope KSCOPE124 UK)/CD2`

### The Sky Moves Sideways - Japan (IECP-20116)  (2)
    - `The Sky Moves Sideways (Disc 1) - Japan (IECP-20116)`
        - artist: Porcupine Tree  ·  year: 2003
        - folder: `D:/music/Porcupine Tree Main discography/Japan Releases/1995 - The Sky Moves Sideways 2CD (WHD Entertainment, Inc. IECP-20116-117 Japan)/CD1`
    - `The Sky Moves Sideways (Disc 2) - Japan (IECP-20117)`
        - artist: Porcupine Tree  ·  year: 2003
        - folder: `D:/music/Porcupine Tree Main discography/Japan Releases/1995 - The Sky Moves Sideways 2CD (WHD Entertainment, Inc. IECP-20116-117 Japan)/CD2`

### The Song Remains The Same  (8)
    - `The Song Remains The Same (CD 1)`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 289 402, Germany)/disc1`
    - `The Song Remains The Same (Disc 1)`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 55XD-568-9, Japan)/disc1`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 7567-90303-2, Germany)/disc1`
    - `The Song Remains The Same (Disc 2)`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 289 402, Germany)/disk2`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song SS 201-2, USA)/disc2`
    - `The Song Remains The Same CD2`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 0603497862757, EU)/Disc 2`
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 7567-90303-2, Germany)/disc2`
    - `The Song Remains The Same(disc 2)`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song AMCY 2439-40, Japan)/disc2`
    - `The Song Remains the Same (CD 1)`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/BoxSets/2008. Definitive Collection (Atlantic-SwanSong R2-513820, Japan-USA)/1976. The Song Remains The Same (Swan Song R2-513936, Japan)/disc1`
    - `The Song Remains the Same (Disc 2)`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 55XD-568-9, Japan)/disk2`
    - `The Song Remains the Same CD1`
        - artist: Led Zeppelin  ·  year: 1976
        - folder: `D:/music/_cd_rip/Led Zeppelin/Live/1976. The Song Remains The Same/1976. Led Zeppelin - The Song Remains The Same (Swan Song 0603497862757, EU)/Disc 1`

### The Soul Of Truth  (2)
    - `The Soul Of Truth CD1`
        - artist: Johnny Cash  ·  year: 2012
        - folder: `D:/music/Cash - Collection/2012. Johnny Cash - Bootleg Vol.IV -  The Soul Of Truth (Columbia-Legacy 88697985382, EU)/CD1`
    - `The Soul Of Truth CD2`
        - artist: Johnny Cash  ·  year: 2012
        - folder: `D:/music/Cash - Collection/2012. Johnny Cash - Bootleg Vol.IV -  The Soul Of Truth (Columbia-Legacy 88697985382, EU)/CD2`

### The Sphinx  (3)
    - `The Sphinx - Disc 1`
        - artist: Amanda Lear  ·  year: 2006
        - folder: `D:/music/_cd_rip/Amanda Lear/23. The Sphinx – 1976-1983 (3CD) (2006)/The Sphinx - Disc 1`
    - `The Sphinx - Disc 2`
        - artist: Amanda Lear  ·  year: 2006
        - folder: `D:/music/_cd_rip/Amanda Lear/23. The Sphinx – 1976-1983 (3CD) (2006)/The Sphinx - Disc 2`
    - `The Sphinx - Disc 3`
        - artist: Amanda Lear - The Sphinx  ·  year: 2006
        - folder: `D:/music/_cd_rip/Amanda Lear/23. The Sphinx – 1976-1983 (3CD) (2006)/The Sphinx - Disc 3`

### The Symphonic  (2)
    - `The Symphonic CD1`
        - artist: The City of Prague Philharmonic Orchestra & Crouch End Festival Chorus  ·  year: 2006
        - folder: `D:/music/_cd_rip/Jean Michel Jarre - Discography/Audio/Tributes/(2006) - The Symphonic/CD1`
    - `The Symphonic CD2`
        - artist: The City of Prague Philharmonic Orchestra & Crouch End Festival Chorus  ·  year: 2006
        - folder: `D:/music/_cd_rip/Jean Michel Jarre - Discography/Audio/Tributes/(2006) - The Symphonic/CD2`

### The Ultimate Collection  (8)
    - `The Ultimate Collection (Disc 1)`
        - artist: Sade  ·  year: 2011
        - folder: `D:/music/Sade - The Ultimate Collection - 2011 (Japan)/CD1`
    - `The Ultimate Collection (Disc 2)`
        - artist: Sade  ·  year: 2011
        - folder: `D:/music/Sade - The Ultimate Collection - 2011 (Japan)/CD2`
    - `The Ultimate Collection (Disc Four)`
        - artist: Michael Jackson  ·  year: 2004
        - folder: `D:/music/Michael Jackson - The Ultimate Collection (2004)`
    - `The Ultimate Collection (Disc One)`
        - artist: Michael Jackson  ·  year: 2004
        - folder: `D:/music/Michael Jackson - The Ultimate Collection (2004)`
    - `The Ultimate Collection (Disc Three)`
        - artist: Michael Jackson  ·  year: 2004
        - folder: `D:/music/Michael Jackson - The Ultimate Collection (2004)`
    - `The Ultimate Collection (Disc Two)`
        - artist: Michael Jackson  ·  year: 2004
        - folder: `D:/music/Michael Jackson - The Ultimate Collection (2004)`
    - `The Ultimate Collection CD1`
        - artist: Sade  ·  year: 2011
        - folder: `D:/music/Sade - The Ultimate Collection (2011) [FLAC]/CD1`
    - `The Ultimate Collection CD2`
        - artist: Sade  ·  year: 2011
        - folder: `D:/music/Sade - The Ultimate Collection (2011) [FLAC]/CD2`

### The Very Best Of  (2)
    - `The Very Best Of CD2`
        - artist: Elton John  ·  year: 1990
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/1990. Elton John - The Very Best Of (Rocket 846947-2, UK)/CD2`
    - `The Very Best of - CD1`
        - artist: Elton John  ·  year: 1990
        - folder: `D:/music/_cd_rip/Sir Elton John/Compilation/1990. Elton John - The Very Best Of (Rocket 846947-2, UK)/CD1`

### The Very Best Of Pop Music 1967-68  (2)
    - `The Very Best Of Pop Music 1967-68 (Disc 1)`
        - artist: Kinks, Hoolies, Manfred Man  ·  year: 1997
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1967-68) [FLAC] (2CD)/CD1`
    - `The Very Best of Pop Music 1967-68 (Disc 2)`
        - artist: Love Affair, The Monkees, Scott McKenzie  ·  year: 1997
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1967-68) [FLAC] (2CD)/CD2`

### The Very Best Of Pop Music 1968-69  (2)
    - `The Very Best Of Pop Music 1968-69 CD 1`
        - artist: Manfred Mann, Foundations, Beach Boys  ·  year: 1997
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1968-69) [FLAC] (2CD)/CD1`
    - `The very best of pop music 1968-69 CD 2`
        - artist: Cliff Richard, Otis Redding, Aretha Franklin  ·  year: 1997
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1968-69) [FLAC] (2CD)/CD2`

### The Very Best Of Pop Music 1969-70  (2)
    - `The Very Best Of Pop Music 1969-70 CD 1`
        - artist: Fifth Dimension, Zager & Evans, Desmond Dekker & The Aces  ·  year: 1997
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1969-70) [FLAC] (2CD)/CD1`
    - `The very best of pop music 1969-70 CD 2`
        - artist: Lulu, Beach Boys, Hollies  ·  year: 1997
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1969-70) [FLAC] (2CD)/CD2`

### The Very Best Of Pop Music 1985-86  (2)
    - `The Very Best Of Pop Music 1985-86 (CD 1)`
        - artist: A-Ha, DeBarge, Katarina & The Waves  ·  year: 1995
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1985-86) [FLAC] (2CD)/CD1`
    - `The very best of pop music 1985-86, disc 2`
        - artist: Tina Turner, Huey Lewis & The News, Foreigner  ·  year: 1995
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1985-86) [FLAC] (2CD)/CD2`

### The Very Best Of Pop Music 1986-87  (2)
    - `The Very Best Of Pop Music 1986-87 CD 1`
        - artist: a-ha, Sly Fox, Boy George  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1986-87) [FLAC] (2CD)/CD1`
    - `The Very Best Of Pop Music 1986-87 CD 2`
        - artist: John Farnham, Eurythmics, Climie Fisher  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1986-87) [FLAC] (2CD)/CD2`

### The Very Best Of Pop Music 1987-88  (2)
    - `The Very Best Of Pop Music 1987-88 CD 1`
        - artist: INXS, Living In A Box, Taylor Dayne  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1987-88) [FLAC] (2CD)/CD1`
    - `The Very Best Of Pop Music 1987-88 CD 2`
        - artist: a-ha, T'Pau, Climie Fisher  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1987-88) [FLAC] (2CD)/CD2`

### The Very Best Of Pop Music 1988-89  (2)
    - `The Very Best Of Pop Music 1988-89 CD 1`
        - artist: A-Ha, Rick Astley, Paula Abdul  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1988-89) [FLAC] (2CD)/CD1`
    - `The Very Best Of Pop Music 1988-89 CD 2`
        - artist: Fairground Attraction, The Christians, Bananarama  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1988-89) [FLAC] (2CD)/CD2`

### The Very Best Of Pop Music 1989-90  (2)
    - `The Very Best Of Pop Music 1989-90 CD 1`
        - artist: Texas, Londonbeat, Cliff Richard  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1989-90) [FLAC] (2CD)/CD1`
    - `The Very Best Of Pop Music 1989-90 CD 2`
        - artist: INXS, Beats International, Cyndi Lauper  ·  year: 1995
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1989-90) [FLAC] (2CD)/CD2`

### The very best of pop music 1990-91  (2)
    - `The very best of pop music 1990-91 CD 1`
        - artist: Joe Cocker, Incognito, Black Box  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1990-91) [FLAC] (2CD)/The very best of pop music 1990-91 CD1`
    - `The very best of pop music 1990-91 CD 2`
        - artist: A-Ha, Fleetwood Mac, Olivia Newton-John & John Travolta  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1990-91) [FLAC] (2CD)/The very best of pop music 1990-91 CD2`

### The very best of pop music 1991-92  (2)
    - `The very best of pop music 1991-92 CD 1`
        - artist: Mr. Big, Inner Circle, Dr. Alban  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1991-92) [FLAC] (2CD)/The very best of pop music 1991-92 CD1`
    - `The very best of pop music 1991-92 CD 2`
        - artist: Scorpions, Annie Lennox, Crowded House  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1991-92) [FLAC] (2CD)/The very best of pop music 1991-92 CD2`

### The Very Best Of Pop Music 1994-95  (2)
    - `The Very Best Of Pop Music 1994-95 CD 1`
        - artist: Me & My, Stakka Bo, Glenmark  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1994-95) [FLAC] (2CD)/Various - The Very Best Of Pop Music 1994-95 CD 1`
    - `The Very Best Of Pop Music 1994-95 CD 2`
        - artist: Sound Of Seduction, Shaggy feat. Rayvon, Everything But The Girl  ·  year: 1996
        - folder: `D:/music/_cd_rip/The Very Best Of Pop Music/Various Artists - The Very Best Of Pop Music (1994-95) [FLAC] (2CD)/Various - The Very Best Of Pop Music 1994-95 CD 2`

### The Wall  (18)
    - `The Wall (CD 1)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[7243 8 31243 2 9] [Holland, Rem 1994]/Disc 1`
    - `The Wall (CD1)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[5099902944623] [EU, 2012 Experience]/CD1`
    - `The Wall (CD2)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[5099902944623] [EU, 2012 Experience]/CD2`
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[7243 8 31243 2 9] [Holland, Rem 1994]/Disc 2`
    - `The Wall (Disc 1)`
        - artist: Pink Floyd  ·  year: 1997, 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[489101 2] [Australia, Rem 1997]/CD1`
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[UDCD 2-537] [USA, MFSL UltraDisc]/Disc 1`
    - `The Wall (Disc 1) (EU Shine On)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[0777 7 80569-70] [UK, Shine On Box Set]/The Wall Part One`
    - `The Wall (Disc 2)`
        - artist: Pink Floyd  ·  year: 1979, 1997
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(CDS 7 46036 8) [4th UK]/Disc 2`
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[489101 2] [Australia, Rem 1997]/CD2`
    - `The Wall (Disc 2) (EU Shine On)`
        - artist: Pink Floyd  ·  year: 1992
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[0777 7 80569-70] [UK, Shine On Box Set]/The Wall Part Two`
    - `The Wall (Disc One)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(CDCBS 88485) [7th Australia]/CD1`
    - `The Wall (Disc1)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(CDS 7 46036 8) [4th UK]/Disc 1`
    - `The Wall (DiscTwo)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(CDCBS 88485) [7th Australia]/CD2`
    - `The Wall - Disc 1`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(C2K 36183) [6th USA]/CD1`
    - `The Wall - Disc 2`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(C2K 36183) [6th USA]/CD2`
    - `The Wall CD1`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[SRCS 8485-86] [5th Japan, Rem 1998]/CD1`
    - `The Wall CD2`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[SRCS 8485-86] [5th Japan, Rem 1998]/CD2`
    - `The Wall Disc 1`
        - artist: Pink Floyd  ·  year: 1992
        - folder: `D:/music/Pink Floyd/BoxSet/1992 Shine on (1st USA)/1979. The Wall Part One (Columbia CK 53187, USA)`
    - `The Wall Disc 2`
        - artist: Pink Floyd  ·  year: 1992
        - folder: `D:/music/Pink Floyd/BoxSet/1992 Shine on (1st USA)/1979. The Wall Part Two (Columbia CK 53188, USA)`
    - `The Wall [8 31243 2] (CD1)`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[7243 8 31243 2 9] [EU, Rem 2009]/CD1`
    - `The Wall [8 31243 2] (CD2)`
        - artist: Pink Floyd  ·  year: 1994
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/[7243 8 31243 2 9] [EU, Rem 2009]/CD2`

### The Wall.Black Face.Japan.  (2)
    - `The Wall.Black Face.Japan.CD1`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(CDP 7 46036 8) [1st UK]/The Wall.CDP7 46036 2.Black Face.Harvest.Japan`
    - `The Wall.Black Face.Japan.CD2`
        - artist: Pink Floyd  ·  year: 1979
        - folder: `D:/music/Pink Floyd/Studio/1979 The Wall/(CDP 7 46036 8) [1st UK]/The Wall.CDP7 46037 2.Black Face.Harvest.Japan`

### The White Album  (2)
    - `The White Album CD1`
        - artist: The Beatles  ·  year: 1968
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2018 - The Beatles (Apple 0602567571957)/Disc 1`
    - `The White Album CD2`
        - artist: The Beatles  ·  year: 1968
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2018 - The Beatles (Apple 0602567571957)/Disc 2`

### The White Album Sessions  (3)
    - `The White Album CD4 Sessions`
        - artist: The Beatles  ·  year: 2018
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2018 - The Beatles (Apple 0602567571957)/Disc 4 Sessions`
    - `The White Album CD5 Sessions`
        - artist: The Beatles  ·  year: 2018
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2018 - The Beatles (Apple 0602567571957)/Disc 5 Sessions`
    - `The White Album CD6 Sessions`
        - artist: The Beatles  ·  year: 2018
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/2018 - The Beatles (Apple 0602567571957)/Disc 6 Sessions`

### The World Is Ours  (2)
    - `The World Is Ours (Disc 2)`
        - artist: Motorhead  ·  year: 2011
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2011-The World Is Ours - Vol.1 (2011, Germany, UDR, UDR 0076 CD, 2CD)/CD1`
    - `The World Is Ours (Disc 3)`
        - artist: Motorhead  ·  year: 2011
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2011-The World Is Ours - Vol.1 (2011, Germany, UDR, UDR 0076 CD, 2CD)/CD2`

### The World Is Ours - Vol.2  (2)
    - `The World Is Ours - Vol.2 (Disc 2)`
        - artist: Motorhead  ·  year: 2012
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2012-The World Is Ours - Vol.2 (2012, Germany, UDR, UDR 0125 DVD+CD, 2CD)/CD1`
    - `The World Is Ours - Vol.2 (Disc 3)`
        - artist: Motorhead  ·  year: 2012
        - folder: `D:/music/_cd_rip/Motorhead/Live Albums/2012-The World Is Ours - Vol.2 (2012, Germany, UDR, UDR 0125 DVD+CD, 2CD)/CD2`

### The X Factor (Japan TOCP-8588)  (2)
    - `The X Factor (Japan TOCP-8588) CD1`
        - artist: Iron Maiden  ·  year: 1995
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1995 The X Factor/1995 The X Factor [1995 Japan TOCP-8588 EMI]/CD 1`
    - `The X Factor (Japan TOCP-8588) CD2`
        - artist: Iron Maiden  ·  year: 1995
        - folder: `D:/music/_cd_rip/Iron Maiden - Дискография/Studio Albums/1995 The X Factor/1995 The X Factor [1995 Japan TOCP-8588 EMI]/CD 2`

### Then & Now (2006, 2CD, SMDDD343)  (2)
    - `Then & Now CD1 (2006, 2CD, SMDDD343)`
        - artist: Emerson, Lake & Palmer  ·  year: 1998
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1998. Emerson, Lake & Palmer - Then & Now (2006, 2CD, Sanctuary, EU, UK, SMDDD343)/CD1`
    - `Then & Now CD2 (2006, 2CD, SMDDD343)`
        - artist: Emerson, Lake & Palmer  ·  year: 1998
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1998. Emerson, Lake & Palmer - Then & Now (2006, 2CD, Sanctuary, EU, UK, SMDDD343)/CD2`

### This Time Around Live In Tokyo 75 (2003, 2CD, VPCK-85326)  (2)
    - `This Time Around Live In Tokyo 75 CD1 (2003, 2CD, VPCK-85326)`
        - artist: Deep Purple  ·  year: 1975
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2001. This Time Around. Live in Tokyo (2003, 2CD, VAP, Japan, VPCK-85326)/CD1`
    - `This Time Around Live In Tokyo 75 CD2 (2003, 2CD, VPCK-85326)`
        - artist: Deep Purple  ·  year: 1975
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2001. This Time Around. Live in Tokyo (2003, 2CD, VAP, Japan, VPCK-85326)/CD2`

### This Time Around. Live In Tokyo (2007, 2CD, PUR321D)  (2)
    - `This Time Around. Live In Tokyo CD1 (2007, 2CD, PUR321D)`
        - artist: Deep Purple  ·  year: 2001
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2001. This Time Around. Live in Tokyo (2007, 2CD, Darker Than Blue Ltd., EU, Germany, PUR321D)/CD1`
    - `This Time Around. Live In Tokyo CD2 (2007, 2CD, PUR321D)`
        - artist: Deep Purple  ·  year: 2001
        - folder: `D:/music/_cd_rip/Deep Purple/Live Albums/2001. This Time Around. Live in Tokyo (2007, 2CD, Darker Than Blue Ltd., EU, Germany, PUR321D)/CD2`

### Three Sides Live (810 006-2)  (2)
    - `Three Sides Live (810 006-2) CD1`
        - artist: Genesis  ·  year: 1982
        - folder: `D:/music/GENESIS Studio Discography (1969-1997)/Albums/1982 - Three Sides Live (810 006-2)/CD 1`
    - `Three Sides Live (810 006-2) CD2`
        - artist: Genesis  ·  year: 1982
        - folder: `D:/music/GENESIS Studio Discography (1969-1997)/Albums/1982 - Three Sides Live (810 006-2)/CD 2`

### Through The Never [UICN-1046]  (2)
    - `Through The Never (Disc 1) [UICN-1046]`
        - artist: Metallica  ·  year: 2013
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/2013 - Through The Never (2 CD)/2013 Japan SHM-CD, UICN-1046~7/Disc 1`
    - `Through The Never (Disc 2) [UICN-1047]`
        - artist: Metallica  ·  year: 2013
        - folder: `D:/music/_cd_rip/Metallica - Discography/Albums/2013 - Through The Never (2 CD)/2013 Japan SHM-CD, UICN-1046~7/Disc 2`

### Thunder In The Sky (MCA 01222-2)  (2)
    - `Thunder In The Sky Disc 1 (MCA 01222-2)`
        - artist: Manowar  ·  year: 2009
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/EP and Singles/2009 Thunder In The Sky [Germany MCA 01222-2 MCM]/CD 1`
    - `Thunder In The Sky Disc 2 (MCA 01222-2)`
        - artist: Manowar  ·  year: 2009
        - folder: `D:/music/_cd_rip/Manowar 1982-2014/EP and Singles/2009 Thunder In The Sky [Germany MCA 01222-2 MCM]/CD 2`

### Time (Deluxe Edition)  (2)
    - `Time (Deluxe Edition) CD1`
        - artist: Rod Stewart  ·  year: 2013
        - folder: `D:/music/_cd_rip/ROD STEWART/Rod Stewart - 2013 - Time (Deluxe Edition 2CD) (Capitol 0602537546381 EU)/CD1`
    - `Time (Deluxe Edition) CD2`
        - artist: Rod Stewart  ·  year: 2013
        - folder: `D:/music/_cd_rip/ROD STEWART/Rod Stewart - 2013 - Time (Deluxe Edition 2CD) (Capitol 0602537546381 EU)/CD2`

### To The Moon And Back - 20 Years And Beyond  (2)
    - `To The Moon And Back - 20 Years And Beyond CD1`
        - artist: Blackmore's Night  ·  year: 2017
        - folder: `D:/music/_cd_rip/Blackmore's Night - Japanese Lossless Discography/Blackmore's Night - 2017 To The Moon And Back - 20 Years And Beyond/CD1`
    - `To The Moon And Back - 20 Years And Beyond CD2`
        - artist: Blackmore's Night  ·  year: 2017
        - folder: `D:/music/_cd_rip/Blackmore's Night - Japanese Lossless Discography/Blackmore's Night - 2017 To The Moon And Back - 20 Years And Beyond/CD2`

### Tokyo Tapes (53039-2)  (4)
    - `Tokyo Tapes (53039-2) (Disc 1)`
        - artist: Scorpions  ·  year: 1978
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/1978 Tokyo Tapes/1978 Tokyo Tapes [1993 USA 53039-2 RCA]/CD 1`
    - `Tokyo Tapes (53039-2) (Disc 2)`
        - artist: Scorpions  ·  year: 1978
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Albums/1978 Tokyo Tapes/1978 Tokyo Tapes [1993 USA 53039-2 RCA]/CD 2`
    - `Tokyo Tapes CD1`
        - artist: Scorpions  ·  year: 1978
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Box Set/1974-78 Blu-spec CD Collection [Japan 2010 SICP 20242~48 Sony]/1978 Tokyo Tapes [Japan  SICP-20247~48]/CD 1`
    - `Tokyo Tapes CD2`
        - artist: Scorpions  ·  year: 1978
        - folder: `D:/music/_cd_rip/Scorpions (1972-2015)/Box Set/1974-78 Blu-spec CD Collection [Japan 2010 SICP 20242~48 Sony]/1978 Tokyo Tapes [Japan  SICP-20247~48]/CD 2`

### Totally Stripped Paris (Live At L'Olympia Paris 1995.07.03)  (2)
    - `Totally Stripped Paris (Live At L'Olympia Paris 1995.07.03) CD 1`
        - artist: The Rolling Stones  ·  year: 2017
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2017 Totally Stripped Paris [Ward GQBS-90278-0]/Disc 1`
    - `Totally Stripped Paris (Live At L'Olympia Paris 1995.07.03) CD 2`
        - artist: The Rolling Stones  ·  year: 2017
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2017 Totally Stripped Paris [Ward GQBS-90278-0]/Disc 2`

### Triplicate  (3)
    - `Triplicate CD1`
        - artist: Bob Dylan  ·  year: 2017
        - folder: `D:/music/_cd_rip/Dylan/Studio/2017 - Triplicate/2017. Bob Dylan - Triplicate (Columbia 88985413492, EU)/CD1. 'Til The Sun Goes Down`
    - `Triplicate CD2`
        - artist: Bob Dylan  ·  year: 2017
        - folder: `D:/music/_cd_rip/Dylan/Studio/2017 - Triplicate/2017. Bob Dylan - Triplicate (Columbia 88985413492, EU)/CD2. Devil Dolls`
    - `Triplicate CD3`
        - artist: Bob Dylan  ·  year: 2017
        - folder: `D:/music/_cd_rip/Dylan/Studio/2017 - Triplicate/2017. Bob Dylan - Triplicate (Columbia 88985413492, EU)/CD3. Comin' Home Late`

### Tumbleweed Connection (5 Classic Albums, Box Set)  (2)
    - `Tumbleweed Connection (5 Classic Albums, Box Set) CD2`
        - artist: Elton John  ·  year: 1970
        - folder: `D:/music/_cd_rip/Sir Elton John/BoxSet/2012. Elton John - 5 Classic Albums (1970-1973) (Mercury 00602537069118, EU)/CD2. 1970. Tumbleweed Connection`
    - `Tumbleweed Connection (Disc 1)`
        - artist: Elton John  ·  year: 1970
        - folder: `D:/music/_cd_rip/Sir Elton John/Studio/1970. Tumbleweed Connection/1970. Elton John - Tumbleweed Connection (Mercury B0010839-02, USA)/Disc 1`

### Turbo 30 - Live In Kansas City (2017, Sony, 30th Anniversary 3 CD Deluxe Edition, SICP 5168, Japan)  (2)
    - `Turbo 30 - Live In Kansas City (2017, Sony, 30th Anniversary 3 CD Deluxe Edition, CD 2, SICP 5168, Japan)`
        - artist: Judas Priest  ·  year: 1986
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/1986 - Turbo/2017 - Turbo 30 [Sony, SICP 5167~9, Japan]`
    - `Turbo 30 - Live In Kansas City (2017, Sony, 30th Anniversary 3 CD Deluxe Edition, CD 3, SICP 5169, Japan)`
        - artist: Judas Priest  ·  year: 2017
        - folder: `D:/music/_cd_rip/Judas Priest/Studio Albums/1986 - Turbo/2017 - Turbo 30 [Sony, SICP 5167~9, Japan]`

### U2 Medium, Rare & Remastered  (2)
    - `U2 Medium, Rare & Remastered (CD 1)`
        - artist: U2  ·  year: 2009
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Fan Club Only Releases/2009 - U2 Medium, Rare & Remastered [U2.com4]/CD1`
    - `U2 Medium, Rare & Remastered (CD 2)`
        - artist: U2  ·  year: 2009
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Fan Club Only Releases/2009 - U2 Medium, Rare & Remastered [U2.com4]/CD2`

### U22  (2)
    - `U22 (Disc One)`
        - artist: U2  ·  year: 2012
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Fan Club Only Releases/2012 - U22 - A 22 Track Live Collection From U2360° (2CD) [U2.com7]/CD1`
    - `U22 (Disc Two)`
        - artist: U2  ·  year: 2012
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Fan Club Only Releases/2012 - U22 - A 22 Track Live Collection From U2360° (2CD) [U2.com7]/CD2`

### Ultimate Kylie  (2)
    - `Ultimate Kylie (CD1)`
        - artist: Kylie Minogue  ·  year: 2004
        - folder: `D:/music/_cd_rip/Kylie Minogue Japan CDs/Kylie Minogue - 2004 - Ultimate Kylie (2CD) (TOCP-66344-45)/CD1`
    - `Ultimate Kylie (CD2) (Japan)`
        - artist: Kylie Minogue  ·  year: 2004
        - folder: `D:/music/_cd_rip/Kylie Minogue Japan CDs/Kylie Minogue - 2004 - Ultimate Kylie (2CD) (TOCP-66344-45)/CD2`

### Ultimate Prince  (2)
    - `Ultimate Prince CD2`
        - artist: Prince  ·  year: 2006
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/2006 - Ultimate (compilation)/cd2`
    - `Ultimate Prince [disc 1]`
        - artist: Prince  ·  year: 2006
        - folder: `D:/music/Prince_-_Discography_[torrents.ru]/2006 - Ultimate (compilation)/cd1`

### Ummagumma  (6)
    - `Ummagumma (CD 1)`
        - artist: Pink Floyd  ·  year: 1969
        - folder: `D:/music/Pink Floyd/Studio/1969 Ummagumma/(CDS 7 46404 8) [1st UK]/CD1`
    - `Ummagumma (CD 2)`
        - artist: Pink Floyd  ·  year: 1969
        - folder: `D:/music/Pink Floyd/Studio/1969 Ummagumma/(CDS 7 46404 8) [1st UK]/CD2`
    - `Ummagumma (USA Capitol CDPB 7 46404 2) (Disc 1)`
        - artist: Pink Floyd  ·  year: 1969
        - folder: `D:/music/Pink Floyd/Studio/1969 Ummagumma/(CDPB 7 46404 2) [3rd USA]/CD1`
    - `Ummagumma (USA Capitol CDPB 7 46404 2) (Disc 2)`
        - artist: Pink Floyd  ·  year: 1969
        - folder: `D:/music/Pink Floyd/Studio/1969 Ummagumma/(CDPB 7 46404 2) [3rd USA]/CD2`
    - `Ummagumma CD1`
        - artist: Pink Floyd  ·  year: 1969
        - folder: `D:/music/Pink Floyd/Studio/1969 Ummagumma/(CDS 7 46404 8) [Austria DADC]/Disc 1`
    - `Ummagumma CD2`
        - artist: Richard Wright, David Gilmour, Nick Mason  ·  year: 1969
        - folder: `D:/music/Pink Floyd/Studio/1969 Ummagumma/(CDS 7 46404 8) [Austria DADC]/Disc 2`

### Uncovered  (2)
    - `Uncovered CD 1`
        - artist: Dream Theater  ·  year: 1996
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/1996-Uncovered`
    - `Uncovered CD 2`
        - artist: Dream Theater  ·  year: 1996
        - folder: `D:/music/_cd_rip/Dream Theater/Bootlegs/1996-Uncovered`

### Up The Downstair  (2)
    - `Up The Downstair CD1`
        - artist: Porcupine Tree  ·  year: 1993
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1993 - Up The Downstair 2CD (Remaster 2005) (Kscope KSCOPE133 UK)/CD1`
    - `Up The Downstair CD2`
        - artist: Porcupine Tree  ·  year: 1993
        - folder: `D:/music/Porcupine Tree Main discography/Remasters/1993 - Up The Downstair 2CD (Remaster 2005) (Kscope KSCOPE133 UK)/CD2`

### Uriah Heep Live  (2)
    - `Uriah Heep Live (Disc 1)`
        - artist: Uriah Heep  ·  year: 2003
        - folder: `D:/music/_cd_rip/Uriah Heep - Discography/Live/1973. Live January 1973/1973. Live January 1973 De-Luxe Edition (Sanctuary SMDDD772,EU)/disc1`
    - `Uriah Heep Live (Disc 2)`
        - artist: Uriah Heep  ·  year: 2003
        - folder: `D:/music/_cd_rip/Uriah Heep - Discography/Live/1973. Live January 1973/1973. Live January 1973 De-Luxe Edition (Sanctuary SMDDD772,EU)/disc2`

### Voodoo Lounge in Japan 1995  (2)
    - `Voodoo Lounge in Japan 1995 (CD1)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Voodoo Lounge In Japan 1995 [UIBY-15094]/CD1`
    - `Voodoo Lounge in Japan 1995 (CD2)`
        - artist: The Rolling Stones  ·  year: 2019
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2019 Voodoo Lounge In Japan 1995 [UIBY-15094]/CD2`

### Voodoo Lounge Uncut  (2)
    - `Voodoo Lounge Uncut (CD1)`
        - artist: The Rolling Stones, Whoopi Goldberg, The Rolling Stones feat. Sheryl Crow  ·  year: 2018
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2018 Voodoo Lounge Uncut [EAGDV101]/CD1`
    - `Voodoo Lounge Uncut (CD2)`
        - artist: The Rolling Stones  ·  year: 2018
        - folder: `D:/music/_cd_rip/Rolling Stones - Discography - 1964-2019/2 Live/2018 Voodoo Lounge Uncut [EAGDV101]/CD2`

### We Sold Our Soul For Rock 'n' Roll  (2)
    - `We Sold Our Soul For Rock 'n' Roll (CD1)`
        - artist: Black Sabbath  ·  year: 1975
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Compilations/1975 We Sold Our Soul For Rock'n'Roll/1975 We Sold Our Soul For Rock'n'Roll [2004 EU SMDDD078 Sanctuary]/CD 1`
    - `We Sold Our Soul For Rock 'n' Roll (CD2)`
        - artist: Black Sabbath  ·  year: 1975
        - folder: `D:/music/_cd_rip/Black Sabbath 1970-2016/Compilations/1975 We Sold Our Soul For Rock'n'Roll/1975 We Sold Our Soul For Rock'n'Roll [2004 EU SMDDD078 Sanctuary]/CD 2`

### Weihnachtsstimmung mit James Last  (2)
    - `Weihnachtsstimmung mit James Last (CD1)`
        - artist: James Last  ·  year: 1997
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/1997 - Weihnachtsstimmung mit James Last  [Polydor 000 661-2] 2CD/CD1`
    - `Weihnachtsstimmung mit James Last (CD2)`
        - artist: James Last  ·  year: 1997
        - folder: `D:/music/JAMES LAST - COLLECTION [FLAC - EZ CD Rip]/1997 - Weihnachtsstimmung mit James Last  [Polydor 000 661-2] 2CD/CD2`

### Welcome Back My Friends, To The Show That Never Ends - Ladies and Gentlemen (2011, 886978301420)  (2)
    - `Welcome Back My Friends, To The Show That Never Ends - Ladies and Gentlemen, CD1 (2011, 886978301420)`
        - artist: Emerson, Lake & Palmer  ·  year: 1974
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1974. Emerson, Lake & Palmer - Welcome Back My Friends... (2011, 2CD, Sony, EU, Germany, 886978301420)/CD1`
    - `Welcome Back My Friends, To The Show That Never Ends - Ladies and Gentlemen, CD2 (2011, 886978301420)`
        - artist: Emerson, Lake & Palmer  ·  year: 1974
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1974. Emerson, Lake & Palmer - Welcome Back My Friends... (2011, 2CD, Sony, EU, Germany, 886978301420)/CD2`

### Welcome Back My Friends, To The Show That Never Ends - Ladies and Gentlemen 2016 Remaster (2016, 2CD, BMGCAT2CD7)  (2)
    - `Welcome Back My Friends, To The Show That Never Ends - Ladies and Gentlemen, CD1 2016 Remaster (2016, 2CD, BMGCAT2CD7)`
        - artist: Emerson, Lake & Palmer  ·  year: 1974
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1974. Emerson, Lake & Palmer - Welcome Back My Friends... (2016, 2CD, BMG, EU, UK-Austria, BMGCAT2CD7)/CD1`
    - `Welcome Back My Friends, To The Show That Never Ends - Ladies and Gentlemen, CD2 2016 Remaster (2016, 2CD, BMGCAT2CD7)`
        - artist: Emerson, Lake & Palmer  ·  year: 1974
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1974. Emerson, Lake & Palmer - Welcome Back My Friends... (2016, 2CD, BMG, EU, UK-Austria, BMGCAT2CD7)/CD2`

### Weld  (2)
    - `Weld (Disc 02)`
        - artist: Neil Young & Crazy Horse  ·  year: 1991
        - folder: `D:/music/_cd_rip/Neil Young/1991. Neil Young - Weld (Reprise 7599-26671-2, Germany)/Disc 2`
    - `Weld [Disc 1]`
        - artist: Young, Neil & Crazy Horse  ·  year: 1991
        - folder: `D:/music/_cd_rip/Neil Young/1991. Neil Young - Weld (Reprise 7599-26671-2, Germany)/Disc 1`

### Who's Next  (2)
    - `Who's Next (Disc 1)`
        - artist: The Who  ·  year: 1971
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1971 - Who's Next (2 CD Deluxe Edition MCA 088113056-2)/Disc 1`
    - `Who's Next (Disc 2)`
        - artist: The Who  ·  year: 1971
        - folder: `D:/music/_cd_rip/The Who - Full Discography (Official Releases)/The Who - 1971 - Who's Next (2 CD Deluxe Edition MCA 088113056-2)/Disc 2`

### Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive  (36)
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 1`
        - artist: Richie Havens, Sri Swami Satchidananda, John Morris  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 1`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 10`
        - artist: Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 10`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 11`
        - artist: Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 11`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 12`
        - artist: Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 12`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 13`
        - artist: Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 13`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 14`
        - artist: Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 14`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 15`
        - artist: Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 15`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 16`
        - artist: Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 16`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 17`
        - artist: Various Artists, various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 17`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 18`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 18`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 19`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 19`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 2`
        - artist: Sweetwater, John Morris, Alex Del Zoppo  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 2`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 20`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 20`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 22`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 21`
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 22`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 23`
        - artist: various artists, Various Artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 23`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 24`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 24`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 25`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 25`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 26`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 26`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 27`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 27`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 28`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 28`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 29`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 29`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 3`
        - artist: Bert Sommer, John Morris  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 3`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 30`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 30`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 31`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 31`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 32`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 32`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 33`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 33`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 34`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 34`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 35`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 35`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 36`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 36`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 37`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 3/CD 37`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 4`
        - artist: various artists, Tim Hardin, John Morris  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 4`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 5`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 5`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 6`
        - artist: Melanie, John Morri  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 6`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 7`
        - artist: Arlo Guthrie, John Morris  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 7`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 8`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 1/CD 8`
    - `Woodstock - Back To The Garden: The Definitive 50th Anniversary Archive CD 9`
        - artist: various artists  ·  year: 2019
        - folder: `D:/music/VA - Woodstock - Back To The Garden The Definitive 50th Anniversary Archive (Rhino, 2019)/Day 2/CD 9`

### Working Class Hero - The Definitive Lennon  (2)
    - `Working Class Hero - The Definitive Lennon (Disc 1)`
        - artist: John Lennon  ·  year: 2005
        - folder: `D:/music/_cd_rip/1969 - 2010 John Lennon/2005 Working Class Hero (0946 3 40080 2 0)/Disc 1`
    - `Working Class Hero - The Definitive Lennon (Disc 2)`
        - artist: John Lennon  ·  year: 2005
        - folder: `D:/music/_cd_rip/1969 - 2010 John Lennon/2005 Working Class Hero (0946 3 40080 2 0)/Disc 2`

### Works Live (2011, 2CD, 886978486523)  (2)
    - `Works Live CD1 (2011, 2CD, 886978486523)`
        - artist: Emerson Lake & Palmer  ·  year: 1978
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1979. Emerson, Lake & Palmer - Works Live (2011, 2CD, Sony, EU, France, 886978486523)/CD1`
    - `Works Live CD2 (2011, 2CD, 886978486523)`
        - artist: Emerson, Lake & Palmer  ·  year: 1978
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Live Albums/1979. Emerson, Lake & Palmer - Works Live (2011, 2CD, Sony, EU, France, 886978486523)/CD2`

### Works Volume 1 (2011, 2CD, 88697848632)  (2)
    - `Works Volume 1 (2011, 2CD, 88697848632) CD1`
        - artist: Emerson, Lake & Palmer  ·  year: 1977
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Studio Albums/1977. Emerson, Lake & Palmer - Works Volume 1 (2011, 2CD, Sony, EU, France, 88697848632)/CD1`
    - `Works Volume 1 (2011, 2CD, 88697848632) CD2`
        - artist: Emerson, Lake & Palmer  ·  year: 1977
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Studio Albums/1977. Emerson, Lake & Palmer - Works Volume 1 (2011, 2CD, Sony, EU, France, 88697848632)/CD2`

### Works Volume I (1987, 55XD-668-9)  (2)
    - `Works Volume I CD1 (1987, 55XD-668-9)`
        - artist: Emerson, Lake & Palmer  ·  year: 1977
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Studio Albums/1977. Emerson, Lake & Palmer - Works Volume 1 (1987, 2CD, Warner-Pioneer, Atlantic, Japan, 55XD-668-9)/CD1`
    - `Works Volume I CD2 (1987, 55XD-668-9)`
        - artist: Emerson, Lake & Palmer  ·  year: 1977
        - folder: `D:/music/_cd_rip/Emerson, Lake & Palmer/Studio Albums/1977. Emerson, Lake & Palmer - Works Volume 1 (1987, 2CD, Warner-Pioneer, Atlantic, Japan, 55XD-668-9)/CD2`

### World Love Sounds  (5)
    - `World Love Sounds (Disc 1)`
        - artist: Paul Mauriat  ·  year: 1998
        - folder: `D:/music/Paul Mauriat (FLAC)/1998. World Love Sounds (5 CD)/Disc 1`
    - `World Love Sounds (Disc 2)`
        - artist: Paul Mauriat  ·  year: 1998
        - folder: `D:/music/Paul Mauriat (FLAC)/1998. World Love Sounds (5 CD)/Disc 2`
    - `World Love Sounds (Disc 3)`
        - artist: Paul Mauriat  ·  year: 1998
        - folder: `D:/music/Paul Mauriat (FLAC)/1998. World Love Sounds (5 CD)/Disc 3`
    - `World Love Sounds (Disc 4)`
        - artist: Paul Mauriat  ·  year: 1998
        - folder: `D:/music/Paul Mauriat (FLAC)/1998. World Love Sounds (5 CD)/Disc 4`
    - `World Love Sounds (Disc 5)`
        - artist: Paul Mauriat  ·  year: 1998
        - folder: `D:/music/Paul Mauriat (FLAC)/1998. World Love Sounds (5 CD)/Disc 5`

### Yellow Submarine (MFSL SuperVinyl 24/96)  (2)
    - `Yellow Submarine  (MFSL SuperVinyl 24/96)`
        - artist: The Beatles  ·  year: 1969
        - folder: `D:/music/The Beatles - The Collection [Mobile Fidelity Sound Labs - MFSL] (24bit.96kHz Vinyl Rip)/11. 1969 - Yellow Submarine {24bit.96kHz Vinyl Rip MFSL 1-108}`
    - `Yellow Submarine (Disc 12)`
        - artist: Beatles, The  ·  year: 1992
        - folder: `D:/music/_cd_rip/Beatles - Discography - 1963-2023/4 Box Sets/1992 - CD Singles Collection/12 Yellow Submarine`

### ZOO TV Tour From Sydney  (2)
    - `ZOO TV Tour From Sydney - Disc1`
        - artist: U2  ·  year: 1993
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Fan Club Only Releases/2006 - Zoo TV Live [U2.com2]/CD1`
    - `ZOO TV Tour From Sydney - Disc2`
        - artist: U2  ·  year: 1993
        - folder: `D:/music/_cd_rip/U2 - CD Discography [FLAC]/Fan Club Only Releases/2006 - Zoo TV Live [U2.com2]/CD2`

### Жёлтые Воды. Акустика `87 (Maschina Records, MASHCD290)  (2)
    - `Жёлтые Воды. Акустика `87 (Maschina Records, MASHCD290) CD1`
        - artist: Виктор Цой  ·  year: 1987
        - folder: `D:/music/Кино ● Каталог Maschina Records/1987 ● Жёлтые Воды. Акустика `87 (2024, Maschina Records, MASHCD290, 2CD)`
    - `Жёлтые Воды. Акустика `87 (Maschina Records, MASHCD290) CD2`
        - artist: Виктор Цой  ·  year: 1987
        - folder: `D:/music/Кино ● Каталог Maschina Records/1987 ● Жёлтые Воды. Акустика `87 (2024, Maschina Records, MASHCD290, 2CD)`

### Играйте дома [2005, Отделение Выход, В 43]  (2)
    - `Играйте дома (Диск A) [2005, Отделение Выход, В 43]`
        - artist: Зоопарк  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984-85 - Майк - Играйте дома [2005, В 43_44]`
    - `Играйте дома (Диск B) [2005, Отделение Выход, В 44]`
        - artist: Зоопарк  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984-85 - Майк - Играйте дома [2005, В 43_44]`

### Исполнение разрешено [1998, Петербургский Рок-клуб, TReC 3-002]  (2)
    - `Исполнение разрешено (CD1) [1998, Петербургский Рок-клуб, TReC 3-002]`
        - artist: БГ, Майк, Цой  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984 - БГ, Майк, Цой - Исполнение разрешено [1998, TReC 3-002_3-003]`
    - `Исполнение разрешено (CD2) [1998, Петербургский Рок-клуб, TReC 3-003]`
        - artist: Цой, БГ, Майк  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984 - БГ, Майк, Цой - Исполнение разрешено [1998, TReC 3-002_3-003]`

### Исцеляющая сила музыки  (3)
    - `Исцеляющая сила музыки CD1`
        - artist: Ренато Анселми, Розенштейн/Вагенер, Кристиан Ланкто  ·  year: 2007
        - folder: `D:/music/Music_dla_Dushi_[torrents.ru]/Исцеляющая сила музыки/Мягкие звуки для ароматерапии (CD 1)`
    - `Исцеляющая сила музыки CD2`
        - artist: Ренато Анселми, Марк Хеббелинк, Джон Францис  ·  year: 2007
        - folder: `D:/music/Music_dla_Dushi_[torrents.ru]/Исцеляющая сила музыки/Ощущения довольства и благоденствия (CD 2)`
    - `Исцеляющая сила музыки CD3`
        - artist: Джим Оливер, Джеффри Томсон  ·  year: 2007
        - folder: `D:/music/Music_dla_Dushi_[torrents.ru]/Исцеляющая сила музыки/Гармоничный резонанс, Внутренний танец (CD 3)`

### Ленинград 1984 [2009, Отделение Выход, В 139]  (2)
    - `Ленинград 1984 (Диск 1) [2009, Отделение Выход, В 139]`
        - artist: Зоопарк  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984 - Майк и Цой - Ленинград 1984 [2009, 2CD, В 139-140]`
    - `Ленинград 1984 (Диск 2) [2009, Отделение Выход, В 140]`
        - artist: Майк и Цой  ·  year: 1984
        - folder: `D:/music/Зоопарк/2. Концертные записи/1984 - Майк и Цой - Ленинград 1984 [2009, 2CD, В 139-140]`

### Москва 1985 [2012, Отделение Выход, В 329]  (2)
    - `Москва 1985 (Диск 1) [2012, Отделение Выход, В 329]`
        - artist: Зоопарк  ·  year: 1985
        - folder: `D:/music/Зоопарк/2. Концертные записи/1985 - Майк и Цой - Москва 1985 [2012, В 329_330]`
    - `Москва 1985 (Диск 2) [2012, Отделение Выход, В 330]`
        - artist: Зоопарк  ·  year: 1985
        - folder: `D:/music/Зоопарк/2. Концертные записи/1985 - Майк и Цой - Москва 1985 [2012, В 329_330]`

### Симфоник (Maschina Records, MASHCD-017, Double CD)  (2)
    - `Симфоник (Maschina Records, MASHCD-017, Double CD) CD1`
        - artist: Симфоническое Кино  ·  year: 2016
        - folder: `D:/music/Юрий Каспарян ● Дискография/2016 ● Симфоник (2018, Maschina Records, MASHCD-017, Double CD)`
    - `Симфоник (Maschina Records, MASHCD-017, Double CD) CD2`
        - artist: Симфоническое Кино  ·  year: 2016
        - folder: `D:/music/Юрий Каспарян ● Дискография/2016 ● Симфоник (2018, Maschina Records, MASHCD-017, Double CD)`

### Сладкая N и другие  (3)
    - `Сладкая N  и другие`
        - artist: Зоопарк  ·  year: 1980
        - folder: `D:/music/Зоопарк - Майк Науменко/1980. Майк  - Сладкая N  и другие`
    - `Сладкая N и другие (Диск 1) [2001, Отделение Выход, В 134]`
        - artist: Майк  ·  year: 1980
        - folder: `D:/music/Зоопарк/1. Альбомы/1980 - Майк - Сладкая N и другие/1980 - Майк - Сладкая N и другие [2001, 2CD, В 134-135]`
    - `Сладкая N и другие (Диск 2) [2001, Отделение Выход, В 135]`
        - artist: Майк  ·  year: 1980
        - folder: `D:/music/Зоопарк/1. Альбомы/1980 - Майк - Сладкая N и другие/1980 - Майк - Сладкая N и другие [2001, 2CD, В 134-135]`

### №5 on Tour  (2)
    - `№5 on Tour CD1`
        - artist: Mylene Farmer  ·  year: 2009
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2009 - №5 on Tour (2CD)/Mylene Farmer - №5 on Tour CD1`
    - `№5 on Tour CD2`
        - artist: Mylene Farmer  ·  year: 2009
        - folder: `D:/music/_cd_rip/Mylene Farmer/Albums/2009 - №5 on Tour (2CD)/Mylene Farmer - №5 on Tour CD2`

