import { connect } from "react-redux";
import { HashRouter } from "react-router-dom";
import { ConfigProvider, theme as antdTheme } from "antd";
import zhCN from "antd/lib/locale/zh_CN";

import useTheme from "@/hooks/useTheme";
import Router from "@/routers/index";
import AuthRouter from "@/routers/utils/authRouter";

import "./index.scss";

const App = (props: any) => {
	const { assemblySize, themeConfig } = props;
	const { primary, isDark } = themeConfig;

	// 全局使用主题
	useTheme(themeConfig);

	const appTheme = {
		algorithm: isDark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
		token: {
			colorPrimary: primary,
			colorInfo: primary,
			colorBgBase: isDark ? "#12161d" : "#f3f1ec",
			colorBgLayout: isDark ? "#12161d" : "#f3f1ec",
			colorBgContainer: isDark ? "#191f27" : "#ffffff",
			colorBgElevated: isDark ? "#1d2430" : "#ffffff",
			colorBorder: isDark ? "#2a323d" : "#e3ded4",
			colorText: isDark ? "#d6dde8" : "rgba(31, 36, 48, 0.9)",
			colorTextSecondary: isDark ? "#93a0b2" : "#6f7786",
			borderRadius: 10,
			fontFamily: '"PingFang SC", "Microsoft YaHei", sans-serif'
		},
		components: {
			Layout: {
				headerBg: isDark ? "#191f27" : "#ffffff",
				bodyBg: isDark ? "#12161d" : "#f3f1ec",
				siderBg: isDark ? "#141922" : "#f8f5ef",
				triggerBg: isDark ? "#141922" : "#f8f5ef"
			},
			Menu: {
				darkItemBg: isDark ? "#141922" : "#f8f5ef",
				darkSubMenuItemBg: isDark ? "#141922" : "#f8f5ef",
				darkItemColor: isDark ? "#d5dbe6" : "#2e3644",
				darkItemHoverColor: isDark ? "#dff7f4" : "#1f4f55",
				darkItemSelectedBg: isDark ? "rgba(87, 196, 184, 0.18)" : "rgba(42, 111, 106, 0.12)",
				darkItemSelectedColor: isDark ? "#dff7f4" : "#1f4f55",
				darkPopupBg: isDark ? "#141922" : "#f8f5ef"
			},
			Input: {
				activeBorderColor: primary,
				hoverBorderColor: primary
			},
			Button: {
				primaryShadow: isDark ? "0 14px 28px rgba(33, 86, 92, 0.25)" : "0 14px 28px rgba(33, 86, 92, 0.18)"
			},
			Tabs: {
				cardBg: isDark ? "#191f27" : "#ffffff"
			}
		}
	};

	return (
		<HashRouter>
			<ConfigProvider componentSize={assemblySize} locale={zhCN} theme={appTheme}>
				<AuthRouter>
					<Router />
				</AuthRouter>
			</ConfigProvider>
		</HashRouter>
	);
};

const mapStateToProps = (state: any) => state.global;
const mapDispatchToProps = {};
export default connect(mapStateToProps, mapDispatchToProps)(App);
