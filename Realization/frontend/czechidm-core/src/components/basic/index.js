import AbstractComponent from './AbstractComponent/AbstractComponent';
import AbstractContextComponent
from './AbstractContextComponent/AbstractContextComponent';
import AbstractContent from './AbstractContent/AbstractContent';
import AbstractForm from './AbstractForm/AbstractForm';
import AbstractFormComponent
from './AbstractFormComponent/AbstractFormComponent';
import BasicForm from './BasicForm/BasicForm';
import Checkbox from './Checkbox/Checkbox';
import SelectBox from './SelectBox/SelectBox';
import EnumSelectBox from './EnumSelectBox/EnumSelectBox';
import BooleanSelectBox from './BooleanSelectBox/BooleanSelectBox';
import TextArea from './TextArea/TextArea';
import TextField from './TextField/TextField';
import FlashMessage from './FlashMessages/FlashMessage';
import FlashMessages from './FlashMessages/FlashMessages';
import HelpIcon from './HelpIcon/HelpIcon';
import Icon from './Icon/Icon';
import Loading from './Loading/Loading';
import Panel from './Panel/Panel';
import PanelHeader from './Panel/PanelHeader';
import PanelBody from './Panel/PanelBody';
import PanelFooter from './Panel/PanelFooter';
import Toolbar from './Toolbar/Toolbar';
import { BasicTable } from './Table';
import Button from './Button/Button';
import SplitButton from './Button/SplitButton';
import MenuItem from './Button/MenuItem';
import DateTimePicker from './DateTimePicker/DateTimePicker';
import Alert from './Alert/Alert';
import Label from './Label/Label';
import Badge from './Badge/Badge';
import Modal from './Modal/Modal';
import ProgressBar from './ProgressBar/ProgressBar';
import Confirm from './Confirm/Confirm';
import Row from './Row/Row';
import Col from './Row/Column';
import Well from './Well/Well';
import Tabs from './Tabs/Tabs';
import Tab from './Tabs/Tab';
import PageHeader from './PageHeader/PageHeader';
import ContentHeader from './ContentHeader/ContentHeader';
import LabelWrapper from './LabelWrapper/LabelWrapper';
import DateValue from './DateValue/DateValue';
import EnumValue from './EnumValue/EnumValue';
import EnumLabel from './EnumLabel/EnumLabel';
import Collapse from './Collapse/Collapse';
import Tooltip from './Tooltip/Tooltip';
import Dropzone from './Dropzone/Dropzone';
import Popover from './Popover/Popover';
import ScriptArea from './ScriptArea/ScriptArea';
import PasswordStrength from './PasswordStrength/PasswordStrength';
import Link from './Link/Link';
import ShortText from './ShortText/ShortText';
import Div from './Div/Div';
import ToggleSwitch from './ToggleSwitch/ToggleSwitch';
import Wizard from './Wizard/Wizard';
import TimeDuration from './TimeDuration/TimeDuration';
import Avatar from './Avatar/Avatar';
import Fab from './Fab/Fab';
import Container from './Container/Container';
import Typography from './Typography/Typography';
import TextDivider from './TextDivider/TextDivider';
import { Tree } from './Tree';
import { TreeItem } from './TreeItem';

const Components = {
  AbstractComponent,
  AbstractContextComponent,
  AbstractContent,
  AbstractForm,
  AbstractFormComponent,
  BasicForm,
  Checkbox,
  SelectBox,
  EnumSelectBox,
  BooleanSelectBox,
  TextArea,
  TextField,
  DateTimePicker,
  FlashMessage,
  FlashMessages,
  HelpIcon,
  Icon,
  Loading,
  Panel,
  PanelHeader,
  PanelBody,
  PanelFooter,
  Toolbar,
  BasicTable,
  Table: BasicTable.Table,
  Column: BasicTable.Column,
  Cell: BasicTable.Cell,
  SortHeaderCell: BasicTable.SortHeaderCell,
  TextCell: BasicTable.TextCell,
  LinkCell: BasicTable.LinkCell,
  DateCell: BasicTable.DateCell,
  BooleanCell: BasicTable.BooleanCell,
  EnumCell: BasicTable.EnumCell,
  Pagination: BasicTable.Pagination,
  Button,
  SplitButton,
  MenuItem,
  Alert,
  Label,
  Badge,
  Modal,
  ProgressBar,
  Confirm,
  Row,
  Col,
  Well,
  Tabs,
  Tab,
  PageHeader,
  ContentHeader,
  LabelWrapper,
  DateValue,
  EnumValue,
  EnumLabel,
  Collapse,
  Tooltip,
  Dropzone,
  Popover,
  ScriptArea,
  PasswordStrength,
  Link,
  ShortText,
  Tree,
  TreeItem,
  Div,
  ToggleSwitch,
  Wizard,
  TimeDuration,
  Avatar,
  Fab,
  Container,
  Typography,
  TextDivider
};

Components.version = '12.0.0';
module.exports = Components;
