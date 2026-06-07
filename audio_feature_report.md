# Audio File Compression — Feature Report

## What it does

A new **"Audio Lab"** tab sits alongside the existing Image Lab. It lets you:

- Open a WAV/AIFF/AU file (toolbar or drag-and-drop) and preview it — waveform
  view, play/pause/stop, live position.
- See its properties auto-displayed: file size, duration, sample rate,
  channels, bit rate, encoding.
- Compress it with one of three algorithms, with adjustable parameters.
- Watch the compression run live: progress bar + two charts (compression
  ratio and processing speed over time), and cancel mid-run.
- Decompress the result back into a playable/comparable/saveable buffer.
- See a report (sizes, savings %, ratio, time taken, settings used).
- Save the compressed bitstream to disk in a custom container format (`.pxac`).
- Reset to the original file at any time.

## Algorithms — and why these three

| Algorithm | Idea | Bits per sample |
|---|---|---|
| **Delta Modulation (DM)** | Predict the next sample, encode only whether it went up or down | 1 (fixed) |
| **Adaptive Delta Modulation (ADM)** | Same, but the step size grows/shrinks itself based on recent history — handles loud and quiet passages without retuning | 1 (fixed) |
| **DPCM** | Predict, then encode the *quantized* residual in a chosen number of bits | `quantizationBits` (fixed) |

These three were picked over **Nonlinear Quantization** (memoryless — a
fundamentally different shape that wouldn't fit the same `AudioCodec`
abstraction) and **Predictive Differential Coding** (essentially "DPCM with a
fancier predictor" — would have added code without a new concept). All three
chosen algorithms predict → encode the residual → reconstruct by replaying the
prediction, so they share one interface, one bit-packing helper, and one test
shape — and they form a natural escalation in sophistication that's easy to
explain side by side.

## A deliberate, honest design choice worth knowing about

None of these three algorithms do entropy coding — they're **fixed-rate**
codes. DM/ADM always spend exactly 1 bit per sample; DPCM always spends exactly
`quantizationBits` bits per sample, no matter what the audio contains. That
means the compression ratio (`originalBits / encodedBits`) is a **constant**
for a given algorithm + settings combination — it converges to that constant
within the first few samples and then stays flat.

The live "ratio over time" chart shows exactly that: a quick rise to a flat
line. That's not a bug or a missed opportunity to make the chart "look more
interesting" — it's the true, honest behavior of these algorithms, and it's a
good talking point: *"why doesn't a more complex signal compress better here,
the way an MP3 encoder would?"* (Answer: because MP3 adapts its bit allocation
to the signal — psychoacoustic, variable-rate — and these classroom algorithms
deliberately don't.)

## Other notable decisions

- **Audio I/O via `javax.sound.sampled`** (built into the JDK) — zero new
  dependencies, reads/writes WAV/PCM natively, and `AudioFormat` already
  carries every property the spec asks to display. An MP3 library was
  considered and rejected: MP3 is itself lossy-compressed, so feeding it into
  a raw-PCM algorithm would produce meaningless compression numbers.
- **Custom `.pxac` container format** for the compressed output — unavoidable,
  since none of these algorithms produce a bitstream compatible with any
  standard container (their codes are sub-byte / variable bit width). Every
  real-world codec (FLAC, MP3, …) does the same thing, just with a far more
  elaborate header.
- **`javafx.concurrent.Task`** drives the compression job (on its own
  single-thread executor) rather than the app's existing
  `BackgroundExecutor`/`UpdateCoalescer`, which implement "latest-wins, replace
  the pending job" — right for live slider recompute, wrong for a one-shot job
  that must run to completion, report fine-grained progress, and be
  cancellable. `Task` ships exactly that shape natively.
- **DPCM's round-trip error is provably bounded** by `±step/2` for any input —
  because the decoder replays the encoder's exact internal predictor sequence —
  which let the test suite assert a hard bound rather than an arbitrary
  tolerance. DM/ADM lack a closed-form bound (they can "slope overload" on fast
  transitions), so they're covered by hand-traced known-value tests instead.

## Where to look in the code

- `domain/audio/compression/` — the three codecs, settings, report, container port
- `features/audioworkspace/` — load/play/save/reset (mirrors the Image Lab's workspace)
- `features/audiocompression/` — compress/decompress/save, settings + progress + report UI
- `GUIDE.md` §16 — full walkthrough with file links, for a deeper read
