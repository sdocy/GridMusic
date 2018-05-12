An experiment using gridviews to provide customizable music playlists. Normal playlists must be played in order,
randomly, or by the rules of some unknown so-called genius algorithm. This prototype attempts to give listeners
control over the flow of their playlists, while still retaining some randomness in the playback to keep playback
from getting stale and predictable.

Listeners lay out the songs, artists or albums for their playlist in a grid pattern. Once playback has commenced,
the next song to play will be randomly chosen from adjacent playlist grids (vertically and horizontally adjacent
grids only. If an unplayed choice is not available adjacent to the last played song, a new grid to start from is
randomly chosen.

Current implemented features :
- Pause and resume playback.
- Choose to play a single song per grid or all songs on the grid.
- Tap on a grid to choose it to be played next.
- Reset played grids so that they may be played again.
- Toggle individual grids between played / not-played.
- Settings may be changed while the grid choosing algorithm is running.
