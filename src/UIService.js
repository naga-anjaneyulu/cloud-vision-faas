import axios from 'axios';


class UIService{


    submitUrl(data){

       return axios.post('http://localhost:8080/',{  headers : {
        'Content-Type': 'application/json'
    },
       data})

    }

    


}

export default new UIService()