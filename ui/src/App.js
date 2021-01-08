import logo from './logo.svg';
import './App.css';
import React, {Component} from 'react';

import UIService from './UIService.js';

class App extends Component {

  constructor(props){
    super(props);
    this.state = {
        url : '',
        url2 :'',
        src : '',
        data : {}
    }
}


handleChange = (event) =>{
  this.setState({
      [event.target.name] : event.target.value
  })
  console.log(this.state.url)
  console.log(event.target.name)
  console.log(event.target.value)
 }

 handleChange2 = (event) =>{
  this.setState({
      [event.target.name] : event.target.value
  })
  console.log(this.state.url)
  console.log(event.target.name)
  console.log(event.target.value)
 }


submit = (event) =>{
    
      var obj = {url : this.state.url, op :"df"}
      var res = {}
      console.log(obj)
       UIService.submitUrl(obj).then(  (response) => {
        console.log(response.data)
        this.setState({
          data : response.data,
        src : obj.url }) })}
   
      submit2 = (event) =>{
        var obj = {url : this.state.url2 , op : "dl"}
        var res = {}
        console.log(obj)
         UIService.submitUrl(obj).then(  (response) => {
           console.log(response.data)
          this.setState({
            data : response.data,
            src : obj.url })}) }

  render() {
    const obj = this.state.data
    return (
      <div className="App" >
        <br></br><br></br>
        <h1>Spectral Insights</h1>
        <br></br><br></br><br></br><br></br>
      <div className ="rowC">
   
       Face Emotion Detection URL : <input type ="text" name ="url" value ={this.state.url} onChange ={this.handleChange}/>
       <button className ="my-button" onClick={this.submit}>Submit</button> <br></br>

      LandMark Detection URL : <input type ="text" name ="url2" value ={this.state.url2} onChange ={this.handleChange2}/>
       <button className ="my-button" onClick={this.submit2}>Submit</button> <br></br>
       </div>
       <br></br><br></br><br></br><br></br>
       <img src={this.state.src} alt="Image on Cloud9" width="500" height="600"></img>
       <div>
       {
        
       Object.keys(obj).map(function(keyName, keyIndex) {
      
          return(<p>{keyName} : {obj[keyName]} </p>)
        })}
 
   
       </div>
       
      </div>
    );
  }
}






export default App;