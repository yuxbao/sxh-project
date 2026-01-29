// 定义侧边栏项的类型
export interface SideBarVisit {
  visit: number;
  download: number;
  rate: string;
}

export interface SideBarItem {
  title: string;
  subTitle: string;
  icon: string | null;
  img: string;
  url: string;
  content: string | null;
  items: Array<{
    title: string;
    name: string | null;
    url: string;
    img: string | null;
    time: string | null;
    tags: number[] | null;
    visit: number | SideBarVisit | null;
  }>;
  style: number;
}
