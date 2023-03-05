package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        //create the user with given name and number
        User user = new User(name,mobile);
        users.add(user);
        userPlaylistMap.put(user,new ArrayList<>());
        creatorPlaylistMap.put(user,new Playlist());
        return user;
    }

    public Artist createArtist(String name) {
        //create the artist with given name
        Artist artist = new Artist(name);
        artists.add(artist);
        artistAlbumMap.put(artist,new ArrayList<>());
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        //If the artist does not exist, first create an artist with given name
        //Create an album with given title and artist
        Album album = new Album(title);

        if(!artists.contains(artistName)){
            Artist artist = createArtist(artistName);
        }

        albums.add(album);
        albumSongMap.put(album,new ArrayList<>());

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        //If the album does not exist in database, throw "Album does not exist" exception
        //Create and add the song to respective album
        Song song = new Song(title,length);
        if(!albumSongMap.containsKey(albumName)){
            throw new Exception("Album does not exist");
        }
        else{
            songs.add(song);
            albumSongMap.get(albumName).add(song);
        }

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        //user check
        User user = null;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }
        else {
            List<Song> list = new ArrayList<>();
            for (Song s : songs) {
                if (s.getLength() == length) {
                    list.add(s);
                }
            }
            playlistSongMap.put(playlist,list);
            creatorPlaylistMap.put(user,playlist);

            if(playlistListenerMap.containsKey(playlist))
            {
                playlistListenerMap.get(playlist).add(user);
            }
            else {
                List<User> lt = new ArrayList<>();
                lt.add(user);
                playlistListenerMap.put(playlist,lt);
            }
           return playlist;
        }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        Playlist playlist =new Playlist(title);
        User user = null;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }
        else{
            List<Song> list = new ArrayList<>();
            for(String name : songTitles){
                for(Song s : songs){
                    if(s.getTitle().equals(name)){
                        list.add(s);
                    }
                }
            }
            playlistSongMap.put(playlist,list);
            creatorPlaylistMap.put(user,playlist);
            if(playlistListenerMap.containsKey(playlist))
            {
                playlistListenerMap.get(playlist).add(user);
            }
            else {
                List<User> lt = new ArrayList<>();
                lt.add(user);
                playlistListenerMap.put(playlist,lt);
            }

            playlists.add(playlist);

            return playlist;
        }

    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating

        User user = null;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }
        Playlist playlist = null;
        for(Playlist p : playlists){
            if(p.getTitle().equals(playlistTitle)){
                playlist = p;
                break;
            }
        }

        if(user==null){
            throw new Exception("User does not exist");
        }
        else if(playlist==null){
            throw new Exception("Playlist does not exist");
        }
        else
        {
            if(creatorPlaylistMap.containsKey(user) == false && playlistListenerMap.get(playlist).contains(user) == false)
            {
                if(playlistListenerMap.containsKey(playlist))
                {
                    playlistListenerMap.get(playlist).add(user);
                }
                else {
                    List<User> l = new ArrayList<>();
                    l.add(user);
                    playlistListenerMap.put(playlist,l);
                }
            }
            return playlist;
        }
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User user = null;
        Song song = null;
        for(User u : users)
        {
            if(u.getMobile().equals(mobile))
            {
                user = u;
                break;
            }
        }

        for(Song s : songs)
        {
            if(s.getTitle().equals(songTitle))
            {
                song = s;
                break;
            }
        }

        if(user==null){
            throw new Exception("User does not exist");
        }
        else if (song==null) {
            throw new Exception("Song does not exist");
        }
        else {
            Album alm = null;
            Artist art = null;

            for(Album alb : albumSongMap.keySet())
            {
                if(albumSongMap.get(alb).contains(song))
                {
                    alm = alb;
                    break;
                }
            }

            for(Artist a : artistAlbumMap.keySet())
            {
                if(artistAlbumMap.get(a).contains(alm))
                {
                    art = a;
                    break;
                }
            }

            if(songLikeMap.containsKey(song))
            {
                if(songLikeMap.get(song).contains(user) == false)
                {
                    songLikeMap.get(song).add(user);
                    song.setLikes(song.getLikes()+1);
                    art.setLikes(art.getLikes()+1);

                }

            }
            else {
                List<User> l = new ArrayList<>();
                l.add(user);
                songLikeMap.put(song,l);
                song.setLikes(song.getLikes()+1);
                art.setLikes(art.getLikes()+1);
            }
            return song;
        }
    }

    public String mostPopularArtist() {
        int max = Integer.MIN_VALUE;
        String name ="";
        for(Artist a : artists){
            if(a.getLikes()>max){
                max = a.getLikes();
                name = a.getName();
            }
        }
        return name;
    }

    public String mostPopularSong() {
        int max = Integer.MIN_VALUE;
        String name ="";

        for(Song s : songs){
            if(s.getLikes()>max){
                max = s.getLikes();
                name = s.getTitle();
            }
        }
        return name;
    }
}
