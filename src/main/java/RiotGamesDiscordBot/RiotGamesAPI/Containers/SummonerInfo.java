package RiotGamesDiscordBot.RiotGamesAPI.Containers;

import RiotGamesDiscordBot.Tournament.RoundRobin.Events.Containers.MemberOnMultipleTeamsContainer;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SummonerInfo  {
    @SerializedName("name")
    private String summonerName;

    @SerializedName("summonerLevel")
    private long summonerLevel;

    @SerializedName("puuid")
    private String encryptedPUUID;

    @SerializedName("id")
    private String encryptedSummonerId;

    @SerializedName("revisionDate")
    private long revisionDate;

    @SerializedName("profileIconId")
    private int summonerIconId;

    @SerializedName("accountId")
    private String encryptedAccountId;

    private ArrayList<RankedInfo> rankedInfo;




    /*
    Getters
     */
    public int getSummonerIconId() {
        return summonerIconId;
    }

    public long getRevisionDate() {
        return revisionDate;
    }

    public long getSummonerLevel() {
        return summonerLevel;
    }

    public String getEncryptedAccountId() {
        return encryptedAccountId;
    }

    public String getEncryptedPUUID() {
        return encryptedPUUID;
    }

    public String getEncryptedSummonerId() {
        return encryptedSummonerId;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public ArrayList<RankedInfo> getRankedInfo() {
        return rankedInfo;
    }

    /*
    Setters
     */
    public void setRankedInfo(ArrayList<RankedInfo> rankedInfo) {
        this.rankedInfo = rankedInfo;
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof SummonerInfo) {
            return ((SummonerInfo) object).getSummonerName().equals(this.summonerName);
        }
        else if (object instanceof MemberOnMultipleTeamsContainer) {
            return ((MemberOnMultipleTeamsContainer) object).summonerInfo.equals(this);
        }

        return false;
    }
}
