import type { SimpleUserInfo } from '@/http/ResponseTypes/UserInfoType/SimpleUserInfoType'

export interface RankItem {
  rank: number;
  score: number;
  user: SimpleUserInfo;
}

export interface RankTime {
  type?: number;
  desc?: string;
}

export interface RankInfoResponse {
  time: string | RankTime;
  items: RankItem[];
}
