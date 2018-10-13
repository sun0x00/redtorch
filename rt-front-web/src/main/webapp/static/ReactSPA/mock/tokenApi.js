// import mockjs from 'mockjs';

function tokenLogin(req, res){
  if(req.body.userName==='test'&&req.body.password==='test'){
    res.json({data:"1",status:'success'})
    return;
  }
  
  res.json({data:"1",status:'error'})
}

export default {
  'POST /api/token/login':tokenLogin,
};
