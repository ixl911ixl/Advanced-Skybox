package com.advancedskybox;

public class PlaybackClock
{
    private long pausedMilliseconds = 0;
    private long startedAtMilliseconds = 0;
    private boolean playing = false;

    public void play()
    {
        if (!playing)
        {
            startedAtMilliseconds = System.currentTimeMillis() - pausedMilliseconds;
            playing = true;
        }
    }

    public void pause()
    {
        if (playing)
        {
            pausedMilliseconds = getElapsedMilliseconds();
            playing = false;
        }
    }

    public void restart()
    {
        pausedMilliseconds = 0;
        startedAtMilliseconds = System.currentTimeMillis();
        playing = true;
    }

    public void resetPaused()
    {
        pausedMilliseconds = 0;
        startedAtMilliseconds = System.currentTimeMillis();
        playing = false;
    }

    public void seekByMilliseconds(long deltaMilliseconds)
    {
        seekToMilliseconds(getElapsedMilliseconds() + deltaMilliseconds);
    }

    public void seekToMilliseconds(long targetMilliseconds)
    {
        pausedMilliseconds = Math.max(0, targetMilliseconds);

        if (playing)
        {
            startedAtMilliseconds = System.currentTimeMillis() - pausedMilliseconds;
        }
    }

    public long getElapsedMilliseconds()
    {
        if (playing)
        {
            return Math.max(0, System.currentTimeMillis() - startedAtMilliseconds);
        }

        return pausedMilliseconds;
    }

    public boolean isPlaying()
    {
        return playing;
    }
}
