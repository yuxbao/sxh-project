import { connect } from "react-redux";

import logo from "@/assets/images/logo_small.png";
import logoMd from "@/assets/images/logo.png";
const Logo = (props: any) => {
	const { isCollapse } = props;
	return (
		<div className="logo-box">
			<img src={!isCollapse ? logoMd : logo} alt="logo" className={!isCollapse ? "logo-img" : "logo-img-md"} />
		</div>
	);
};

const mapStateToProps = (state: any) => state.menu;
export default connect(mapStateToProps)(Logo);
