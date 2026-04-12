import { ThemeConfigProp } from "@/redux/interface";
import darkTheme from "@/styles/theme/theme-dark.less";
import defaultTheme from "@/styles/theme/theme-default.less";

/**
 * @description 全局主题设置
 * */
const useTheme = (themeConfig: ThemeConfigProp) => {
	const { weakOrGray, isDark } = themeConfig;
	const initTheme = () => {
		// 灰色和弱色切换
		const root = document.documentElement as HTMLElement;
		root.dataset.themeMode = isDark ? "dark" : "light";
		root.style.removeProperty("filter");
		if (weakOrGray === "weak") root.style.setProperty("filter", "invert(80%)");
		if (weakOrGray === "gray") root.style.setProperty("filter", "grayscale(1)");

		// 切换暗黑模式
		const head = document.head;
		const themeStyles = head.querySelectorAll("style[data-type='theme-mode']");
		themeStyles.forEach(style => style.remove());

		const styleDom = document.createElement("style");
		styleDom.dataset.type = "theme-mode";
		styleDom.innerHTML = isDark ? darkTheme : defaultTheme;
		head.appendChild(styleDom);
	};
	initTheme();

	return {
		initTheme
	};
};

export default useTheme;
